package alebuc.torchlight.controller;

import alebuc.torchlight.configuration.AppConfiguration;
import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.login.Login;
import alebuc.torchlight.model.login.LoginType;
import alebuc.torchlight.store.CorruptedProfileFileException;
import alebuc.torchlight.store.LoginProfileStore;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller of the login screen: lets the user connect to a Kafka cluster, and manage saved
 * connection profiles stored by {@link LoginProfileStore}.
 */
@Slf4j
public class LoginController {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private final LoginProfileStore profileStore = LoginProfileStore.defaultStore();

    @FXML
    private ListView<Login> profileListView;
    @FXML
    private TextField profileName;
    @FXML
    private TextField hostname;
    @FXML
    private TextField port;
    @FXML
    private ChoiceBox<LoginType> loginType;
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField username;
    @FXML
    private Label secretLabel;
    @FXML
    private PasswordField secret;
    @FXML
    private Button deleteProfileButton;
    @FXML
    private Button saveAndConnectButton;
    @FXML
    private Button connectButton;

    @FXML
    public void initialize() {
        loginType.getItems().addAll(LoginType.values());
        loginType.setConverter(new StringConverter<>() {
            @Override
            public String toString(LoginType loginType) {
                return loginType == null ? "" : loginType.getName();
            }

            @Override
            public LoginType fromString(String string) {
                return LoginType.valueOf(string);
            }
        });
        loginType.valueProperty().addListener((observable, oldValue, newValue) -> updateCredentialsFields(newValue));
        loginType.setValue(LoginType.DEFAULT);
        updateCredentialsFields(loginType.getValue());

        profileListView.setPlaceholder(new Label("No saved profiles"));
        profileListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Login login, boolean empty) {
                super.updateItem(login, empty);
                setText(empty || login == null ? null : login.name());
            }
        });
        profileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        fillForm(newValue);
                    }
                });
        deleteProfileButton.disableProperty().bind(profileListView.getSelectionModel().selectedItemProperty().isNull());

        loadProfiles();
    }

    @FXML
    void onConnectButtonAction(ActionEvent event) {
        connect();
    }

    @FXML
    void onSaveAndConnectButtonAction(ActionEvent event) {
        Optional<Login> login = validateForm();
        if (login.isEmpty()) {
            return;
        }
        try {
            profileStore.save(login.get());
        } catch (CorruptedProfileFileException e) {
            showCorruptedFileAlert(e);
            loadProfiles();
            return;
        } catch (IOException e) {
            log.error("Could not save profile '{}'", login.get().name(), e);
            showAlert(Alert.AlertType.ERROR, "Could not save the profile",
                    "The profile could not be written to " + profileStore.getFile() + ":\n" + e.getMessage());
            return;
        }
        connect();
    }

    @FXML
    void onDeleteProfileButtonAction(ActionEvent event) {
        Login selected = profileListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete the profile \"" + selected.name() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirmation.setHeaderText(null);
        confirmation.setTitle("Delete profile");
        confirmation.initOwner(getWindow());
        if (confirmation.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        try {
            profileStore.delete(selected.name());
        } catch (CorruptedProfileFileException e) {
            showCorruptedFileAlert(e);
        } catch (IOException e) {
            log.error("Could not delete profile '{}'", selected.name(), e);
            showAlert(Alert.AlertType.ERROR, "Could not delete the profile",
                    "The profile file " + profileStore.getFile() + " could not be updated:\n" + e.getMessage());
            return;
        }
        loadProfiles();
        clearForm();
    }

    /**
     * Connects to the cluster described by the form, then replaces the login window by the main window.
     * Shared by "Connect" and "Save &amp; connect". The secret is only kept in memory.
     */
    private void connect() {
        setConnectButtonsDisabled(true);
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            KafkaProperties kafkaProperties =
                    new KafkaProperties(getBrokerUrl(), loginType.getValue(), username.getText(), secret.getText());
            context.getBeanFactory().registerSingleton("kafkaProperties", kafkaProperties);
            context.register(AppConfiguration.class);
            context.refresh();

            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ui.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Torchlight");
            stage.setScene(scene);
            Stage loginStage = (Stage) connectButton.getScene().getWindow();
            loginStage.close();
            stage.show();
        } catch (IOException | RuntimeException e) {
            log.error("Connection failed", e);
            setConnectButtonsDisabled(false);
            showAlert(Alert.AlertType.ERROR, "Connection failed", String.valueOf(e.getMessage()));
        }
    }

    private void setConnectButtonsDisabled(boolean disabled) {
        connectButton.setDisable(disabled);
        saveAndConnectButton.setDisable(disabled);
    }

    private String getBrokerUrl() {
        String brokerUrl = hostname.getText().trim();
        if (StringUtils.isNotBlank(port.getText())) {
            brokerUrl += ":" + port.getText().trim();
        }
        return brokerUrl;
    }

    /**
     * Checks the fields required to save a profile.
     *
     * @return the profile to save, or empty if the form is invalid (the user has then been warned)
     */
    private Optional<Login> validateForm() {
        List<String> errors = new ArrayList<>();
        String name = StringUtils.trimToEmpty(profileName.getText());
        String host = StringUtils.trimToEmpty(hostname.getText());
        String portText = StringUtils.trimToEmpty(port.getText());
        if (name.isEmpty()) {
            errors.add("Profile name is required.");
        }
        if (host.isEmpty()) {
            errors.add("Host is required.");
        }
        int portNumber = parsePort(portText);
        if (portNumber < 0) {
            errors.add("Port must be a whole number between %d and %d.".formatted(MIN_PORT, MAX_PORT));
        }
        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "The profile cannot be saved", String.join("\n", errors));
            return Optional.empty();
        }
        LoginType type = Optional.ofNullable(loginType.getValue()).orElse(LoginType.DEFAULT);
        String user = type == LoginType.CLIENT_CREDENTIALS ? StringUtils.trimToNull(username.getText()) : null;
        return Optional.of(new Login(name, host, portNumber, type, user));
    }

    /**
     * @return the port, or {@code -1} if the text is not an integer between {@value MIN_PORT} and {@value MAX_PORT}
     */
    private static int parsePort(String text) {
        try {
            int value = Integer.parseInt(text);
            return value >= MIN_PORT && value <= MAX_PORT ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void loadProfiles() {
        List<Login> profiles;
        try {
            profiles = profileStore.findAll();
        } catch (CorruptedProfileFileException e) {
            profiles = List.of();
            // Deferred so the alert shows up once the login window is displayed.
            Platform.runLater(() -> showCorruptedFileAlert(e));
        } catch (IOException e) {
            log.error("Could not read profiles from {}", profileStore.getFile(), e);
            profiles = List.of();
            String message = "Saved profiles could not be read from " + profileStore.getFile() + ":\n" + e.getMessage();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Could not read saved profiles", message));
        }
        profileListView.getItems().setAll(profiles);
    }

    private void fillForm(Login login) {
        profileName.setText(StringUtils.defaultString(login.name()));
        hostname.setText(StringUtils.defaultString(login.host()));
        port.setText(login.port() > 0 ? String.valueOf(login.port()) : "");
        loginType.setValue(Optional.ofNullable(login.type()).orElse(LoginType.DEFAULT));
        username.setText(StringUtils.defaultString(login.username()));
        secret.clear();
    }

    private void clearForm() {
        profileListView.getSelectionModel().clearSelection();
        profileName.clear();
        hostname.clear();
        port.clear();
        loginType.setValue(LoginType.DEFAULT);
        username.clear();
        secret.clear();
    }

    private void updateCredentialsFields(LoginType type) {
        boolean credentials = type == LoginType.CLIENT_CREDENTIALS;
        for (Node node : List.of(usernameLabel, username, secretLabel, secret)) {
            node.setVisible(credentials);
            node.setManaged(credentials);
        }
        username.setDisable(!credentials);
        secret.setDisable(!credentials);
    }

    private void showCorruptedFileAlert(CorruptedProfileFileException e) {
        showAlert(Alert.AlertType.WARNING, "Saved profiles could not be read",
                "The profile file was unreadable. It has been moved to:\n" + e.getBackupFile()
                        + "\n\nStarting with an empty profile list.");
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle("Torchlight");
        alert.setHeaderText(header);
        Window owner = getWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    private Window getWindow() {
        return connectButton.getScene() == null ? null : connectButton.getScene().getWindow();
    }
}
