package alebuc.torchlight;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;


@Component
public class SimpleUIController {

    private final HostServices services;

    @FXML
    public Label label;

    @FXML
    public Button button;

    public SimpleUIController(HostServices services) {
        this.services = services;
    }

    @FXML
    public void initialize() {
        this.button.setOnAction(actionEvent -> this.label.setText(this.services.getDocumentBase()));
    }
}
