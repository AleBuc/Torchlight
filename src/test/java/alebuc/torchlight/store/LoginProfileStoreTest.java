package alebuc.torchlight.store;

import alebuc.torchlight.model.login.Login;
import alebuc.torchlight.model.login.LoginType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

class LoginProfileStoreTest {

    @TempDir
    Path tempDir;

    private Path file() {
        return tempDir.resolve(".torchlight").resolve("login.json");
    }

    private LoginProfileStore store() {
        return new LoginProfileStore(file());
    }

    @Test
    void findAll_missingDirectoryAndFile_returnsEmptyListWithoutCreatingAnything() throws IOException {
        assertThat(store().findAll()).isEmpty();
        assertThat(file().getParent()).doesNotExist();
    }

    @Test
    void findAll_emptyFile_returnsEmptyList() throws IOException {
        Files.createDirectories(file().getParent());
        Files.createFile(file());

        assertThat(store().findAll()).isEmpty();
        assertThat(file()).exists();
    }

    @Test
    void findAll_blankFile_returnsEmptyList() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), " \n\t");

        assertThat(store().findAll()).isEmpty();
    }

    @Test
    void findAll_jsonNull_returnsEmptyListAndKeepsTheFile() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), "null");

        assertThat(store().findAll()).isEmpty();
        assertThat(file()).hasContent("null");
    }

    @Test
    void save_thenFindAll_roundTripsAllFieldsSortedByName() throws IOException {
        Login local = new Login("local", "localhost", 9092, LoginType.NONE, null);
        Login prod = new Login("Prod", "kafka.example.com", 9093, LoginType.CLIENT_CREDENTIALS, "alice");
        Login acme = new Login("acme", "acme.example.com", 19092, LoginType.NONE, null);

        LoginProfileStore store = store();
        store.save(local);
        store.save(prod);
        store.save(acme);

        assertThat(new LoginProfileStore(file()).findAll()).containsExactly(acme, local, prod);
    }

    @Test
    void save_existingName_replacesProfile() throws IOException {
        LoginProfileStore store = store();
        store.save(new Login("dev", "old-host", 9092, LoginType.NONE, null));
        store.save(new Login("other", "other-host", 9092, LoginType.NONE, null));

        Login updated = new Login("dev", "new-host", 29092, LoginType.CLIENT_CREDENTIALS, "bob");
        store.save(updated);

        assertThat(store.findAll())
                .hasSize(2)
                .contains(updated)
                .filteredOn(login -> login.name().equals("dev")).containsExactly(updated);
    }

    @Test
    void save_blankName_isRejected() {
        assertThatThrownBy(() -> store().save(new Login(" ", "host", 9092, LoginType.NONE, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_removesOnlyTheNamedProfile() throws IOException {
        LoginProfileStore store = store();
        Login keep = new Login("keep", "host", 9092, LoginType.NONE, null);
        store.save(keep);
        store.save(new Login("drop", "host", 9092, LoginType.NONE, null));

        assertThat(store.delete("drop")).isTrue();
        assertThat(store.delete("unknown")).isFalse();

        assertThat(store.findAll()).containsExactly(keep);
    }

    @Test
    void save_neverWritesASecretField() throws IOException {
        store().save(new Login("prod", "host", 9092, LoginType.CLIENT_CREDENTIALS, "alice"));

        String json = Files.readString(file());
        assertThat(json)
                .contains("\"name\"", "\"host\"", "\"port\"", "\"type\"", "\"username\"", "CLIENT_CREDENTIALS")
                .doesNotContainIgnoringCase("secret")
                .doesNotContainIgnoringCase("password");
    }

    @Test
    void findAll_legacyFieldsAreIgnored() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), """
                [{"name":"dev","host":"h","port":1,"type":"NONE","username":null,"secret":"s3cr3t"}]""");

        assertThat(store().findAll()).containsExactly(new Login("dev", "h", 1, LoginType.NONE, null));
    }

    @Test
    void findAll_corruptedJson_movesFileToBakAndThenBehavesAsEmpty() throws IOException {
        Files.createDirectories(file().getParent());
        String garbage = "{ this is not json";
        Files.writeString(file(), garbage);
        LoginProfileStore store = store();

        assertThatThrownBy(store::findAll)
                .isInstanceOf(CorruptedProfileFileException.class)
                .satisfies(e -> assertThat(((CorruptedProfileFileException) e).getBackupFile())
                        .isEqualTo(file().resolveSibling("login.json.bak")));

        assertThat(file().resolveSibling("login.json.bak")).hasContent(garbage);
        assertThat(file()).doesNotExist();
        assertThat(store.findAll()).isEmpty();
    }

    @Test
    void corruptedJson_existingBackupIsNeverOverwritten() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file().resolveSibling("login.json.bak"), "first");
        Files.writeString(file(), "[not json");

        assertThatThrownBy(store()::findAll).isInstanceOf(CorruptedProfileFileException.class);

        assertThat(file().resolveSibling("login.json.bak")).hasContent("first");
        assertThat(file().resolveSibling("login.json.bak.1")).hasContent("[not json");
    }

    @Test
    void save_onCorruptedFile_doesNotOverwriteIt() throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), "[not json");

        assertThatThrownBy(() -> store().save(new Login("dev", "h", 1, LoginType.NONE, null)))
                .isInstanceOf(CorruptedProfileFileException.class);

        assertThat(file()).doesNotExist();
        assertThat(file().resolveSibling("login.json.bak")).hasContent("[not json");
    }

    @Test
    void save_onPosix_restrictsDirectoryAndFilePermissions() throws IOException {
        assumeThat(tempDir.getFileSystem().supportedFileAttributeViews()).contains("posix");

        store().save(new Login("dev", "h", 1, LoginType.NONE, null));

        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(file()))).isEqualTo("rw-------");
        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(file().getParent())))
                .isEqualTo("rwx------");
    }

    @Test
    void save_onPosix_fixesPermissionsOfExistingFile() throws IOException {
        assumeThat(tempDir.getFileSystem().supportedFileAttributeViews()).contains("posix");
        Files.createDirectories(file().getParent());
        Files.writeString(file(), "[]");
        Files.setPosixFilePermissions(file(), PosixFilePermissions.fromString("rw-r--r--"));
        Files.setPosixFilePermissions(file().getParent(), PosixFilePermissions.fromString("rwxr-xr-x"));

        store().save(new Login("dev", "h", 1, LoginType.NONE, null));

        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(file()))).isEqualTo("rw-------");
        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(file().getParent())))
                .isEqualTo("rwx------");
    }

    @Test
    void save_leavesNoTemporaryFileBehind() throws IOException {
        LoginProfileStore store = store();
        store.save(new Login("a", "h", 1, LoginType.NONE, null));
        store.save(new Login("b", "h", 1, LoginType.NONE, null));

        try (var files = Files.list(file().getParent())) {
            assertThat(files).containsExactly(file());
        }
    }
}
