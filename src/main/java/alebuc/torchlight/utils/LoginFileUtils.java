package alebuc.torchlight.utils;

import alebuc.torchlight.model.login.Login;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class for reading and writing {@link Login} data
 * as JSON in a file under the user's home directory.
 */
public class LoginFileUtils {

    private static final String DIRECTORY_PATH = System.getProperty("user.home") +"/.torchlight";
    private static final String FILE_NAME = "login.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads login data from a predefined JSON file and deserializes it into a list of {@link Login} objects.
     * If the file does not exist, it will be created automatically.
     * In case of any I/O errors during file access or deserialization, a {@link RuntimeException} is thrown.
     *
     * @return a list of {@link Login} objects containing the login data retrieved from the JSON file
     * @throws RuntimeException if an error occurs during file access or data deserialization
     */
    public static List<Login> getLoginData() {
        File file = getFile();
        try {
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Login.class));
        } catch (IOException e) {
            throw new RuntimeException("A problem occurred during login data retrieval", e);
        }
    }

    /**
     * Saves a list of login data to a predefined file in JSON format.
     *
     * @param loginData the list of Login objects to be saved
     * @throws RuntimeException if an I/O error occurs during the saving process
     */
    public static void saveLoginData(List<Login> loginData) {
        File file = getFile();
        try {
            objectMapper.writeValue(file, loginData);
        } catch (IOException e) {
            throw new RuntimeException("A problem occurred during login data saving", e);
        }
    }

    private static File getFile() {
        Path directoryPath = Path.of(DIRECTORY_PATH);
        if (Files.notExists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException("A problem occurred during directory creation", e);
            }
        }
        Path filePath = directoryPath.resolve(FILE_NAME);
        if (Files.notExists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException("A problem occurred during login file creation", e);
            }
        }
        return filePath.toFile();
    }
}
