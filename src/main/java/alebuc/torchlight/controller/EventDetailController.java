package alebuc.torchlight.controller;

import alebuc.torchlight.model.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

public class EventDetailController extends TabPane {

    @FXML
    private TextArea keyTextField;
    @FXML
    private TextArea valueTextField;
    @FXML
    private TextArea headersTextField;

    public EventDetailController(Event<?,?> event) {
        this.keyTextField = new TextArea();
        this.valueTextField = new TextArea();
        this.headersTextField = new TextArea();
        this.keyTextField.setText(String.valueOf(event.getKey()));
        this.keyTextField.setEditable(false);
        this.valueTextField.setText(String.valueOf(event.getValue()));
        this.valueTextField.setEditable(false);
        this.headersTextField.setText(String.valueOf(event.getHeaders()));
        this.headersTextField.setEditable(false);
    }
}
