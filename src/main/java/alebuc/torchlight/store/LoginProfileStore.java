package alebuc.torchlight.store;

import alebuc.torchlight.model.login.Login;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Reads and writes connection profiles ({@link Login}) as a JSON array in a single file.
 * <ul>
 *     <li>A missing directory, missing file or empty file is treated as "no profiles".</li>
 *     <li>Writes are atomic: a temporary file is written in the same directory, then moved over the target.</li>
 *     <li>On POSIX file systems, the directory is restricted to {@code 700} and the file to {@code 600}.</li>
 *     <li>An unreadable file is never overwritten: it is moved to {@code <file>.bak} and a
 *     {@link CorruptedProfileFileException} is thrown.</li>
 * </ul>
 * Profiles are identified by their name. Methods are synchronized so a single instance can be shared.
 */
@Slf4j
public class LoginProfileStore {

    private static final Comparator<Login> BY_NAME =
            Comparator.comparing(Login::name, String.CASE_INSENSITIVE_ORDER).thenComparing(Login::name);
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    private final Path file;
    private final ObjectMapper objectMapper;
    private final CollectionType listType;

    /**
     * @param file path of the JSON file holding the profiles; it does not need to exist yet
     */
    public LoginProfileStore(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Login.class);
    }

    /**
     * @return a store backed by {@code ~/.torchlight/login.json}
     */
    public static LoginProfileStore defaultStore() {
        return new LoginProfileStore(Path.of(System.getProperty("user.home"), ".torchlight", "login.json"));
    }

    /**
     * @return the JSON file backing this store
     */
    public Path getFile() {
        return file;
    }

    /**
     * Reads all saved profiles.
     *
     * @return the profiles sorted by name, or an empty list if the file is missing or empty
     * @throws CorruptedProfileFileException if the file is unreadable (it has then been moved aside)
     * @throws IOException                   on any other I/O error
     */
    public synchronized List<Login> findAll() throws IOException {
        return read();
    }

    /**
     * Creates the profile, or replaces the existing profile with the same name, then writes the file.
     *
     * @param login profile to save; its name must not be blank
     * @throws CorruptedProfileFileException if the existing file is unreadable (it has then been moved aside
     *                                       and nothing is written)
     * @throws IOException                   on any other I/O error
     */
    public synchronized void save(Login login) throws IOException {
        Objects.requireNonNull(login, "login");
        if (StringUtils.isBlank(login.name())) {
            throw new IllegalArgumentException("Profile name must not be blank");
        }
        List<Login> profiles = new ArrayList<>(read());
        profiles.removeIf(profile -> profile.name().equals(login.name()));
        profiles.add(login);
        write(profiles);
    }

    /**
     * Deletes the profile with the given name, then writes the file. Does nothing if no such profile exists.
     *
     * @param name name of the profile to delete
     * @return {@code true} if a profile was deleted
     * @throws CorruptedProfileFileException if the existing file is unreadable (it has then been moved aside)
     * @throws IOException                   on any other I/O error
     */
    public synchronized boolean delete(String name) throws IOException {
        List<Login> profiles = new ArrayList<>(read());
        boolean removed = profiles.removeIf(profile -> profile.name().equals(name));
        if (removed) {
            write(profiles);
        }
        return removed;
    }

    private List<Login> read() throws IOException {
        if (Files.notExists(file)) {
            return List.of();
        }
        List<Login> profiles;
        try {
            String content = decode(Files.readAllBytes(file));
            if (content.isBlank()) {
                return List.of();
            }
            profiles = objectMapper.readValue(content, listType);
        } catch (JacksonException | CharacterCodingException e) {
            Path backup = moveAside();
            log.warn("Unreadable profile file {} moved to {}: {}", file, backup, e.getMessage());
            throw new CorruptedProfileFileException(file, backup, e);
        }
        if (profiles == null) {
            return List.of();
        }
        return profiles.stream()
                .filter(Objects::nonNull)
                .filter(profile -> StringUtils.isNotBlank(profile.name()))
                .sorted(BY_NAME)
                .toList();
    }

    private static String decode(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    }

    /**
     * Moves the unreadable file to {@code <file>.bak}, or {@code <file>.bak.N} if previous backups exist,
     * so that no backup is ever overwritten.
     */
    private Path moveAside() throws IOException {
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        for (int i = 1; Files.exists(backup); i++) {
            backup = file.resolveSibling(file.getFileName() + ".bak." + i);
        }
        Files.move(file, backup);
        return backup;
    }

    private void write(List<Login> profiles) throws IOException {
        Path directory = file.getParent();
        boolean posix = isPosix();
        createDirectory(directory, posix);

        byte[] content = objectMapper.writeValueAsBytes(profiles.stream().sorted(BY_NAME).toList());
        FileAttribute<?>[] attributes = posix
                ? new FileAttribute<?>[]{PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS)}
                : new FileAttribute<?>[0];
        Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp", attributes);
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(content);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            if (posix) {
                Files.setPosixFilePermissions(temporary, FILE_PERMISSIONS);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                log.debug("Atomic move not supported in {}, falling back to a regular move", directory);
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void createDirectory(Path directory, boolean posix) throws IOException {
        if (Files.notExists(directory)) {
            if (posix) {
                Files.createDirectories(directory, PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS));
            } else {
                Files.createDirectories(directory);
            }
        }
        if (posix) {
            // The creation attribute is subject to the umask, so apply the permissions explicitly.
            Files.setPosixFilePermissions(directory, DIRECTORY_PERMISSIONS);
        }
    }

    private boolean isPosix() {
        return file.getFileSystem().supportedFileAttributeViews().contains("posix");
    }
}
