package alebuc.torchlight.controller;

import alebuc.torchlight.configuration.AppConfiguration;
import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.login.LoginType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

@Slf4j
public class LoginController extends GridPane {

    @FXML
    private TextField hostname;
    @FXML
    private TextField port;
    @FXML
    private ChoiceBox<LoginType> loginType;
    @FXML
    private VBox clientCredentialsVBox;
    @FXML
    private TextArea username;
    @FXML
    private TextArea secret;
    @FXML
    private Button connectButton;

    public LoginController() {
    }

    @FXML
    public void initialize() {
        loginType.getItems().addAll(LoginType.values());
        loginType.setValue(LoginType.DEFAULT);
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
        loginType.setOnAction(event -> {
            switch (loginType.getValue()) {
                case NONE -> {
                    username.setDisable(true);
                    secret.setDisable(true);
                    clientCredentialsVBox.setVisible(false);
                }
                case CLIENT_CREDENTIALS -> {
                    username.setDisable(false);
                    secret.setDisable(false);
                    clientCredentialsVBox.setVisible(true);
                }
            }
        });
    }

    @FXML
    void onConnectButtonAction(ActionEvent event) throws IOException {
        connectButton.setDisable(true);

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
    }

    private String getBrokerUrl() {
        String brokerUrl = hostname.getText();
        if (StringUtils.isNotBlank(port.getText())) {
            brokerUrl += ":" + port.getText();
        }
        return brokerUrl;
    }
}
