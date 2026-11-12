package alebuc.torchlight.store;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Thrown when the profile file cannot be parsed. By the time this exception is thrown,
 * the unreadable file has already been moved aside to {@link #getBackupFile()}, so the
 * store behaves as if it were empty from then on.
 */
public class CorruptedProfileFileException extends IOException {

    private final transient Path backupFile;

    public CorruptedProfileFileException(Path file, Path backupFile, Throwable cause) {
        super("Profile file '%s' is unreadable and was moved to '%s'".formatted(file, backupFile), cause);
        this.backupFile = backupFile;
    }

    /**
     * @return where the unreadable file has been moved
     */
    public Path getBackupFile() {
        return backupFile;
    }
}
