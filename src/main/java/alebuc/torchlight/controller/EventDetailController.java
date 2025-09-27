package alebuc.torchlight.controller;

import alebuc.torchlight.model.Event;
import alebuc.torchlight.utils.ValueUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventDetailController extends TabPane {

    private final ValueUtils valueUtils;

    @FXML
    private TextArea keyTextField;
    @FXML
    private TextArea valueTextField;
    @FXML
    private TextArea headersTextField;

    public void setTextFields(Event<?,?> event) {
        this.keyTextField.setText(valueUtils.stringOf(event.getKey()));
        this.keyTextField.setEditable(false);
        this.valueTextField.setText(valueUtils.stringOf(event.getValue()));
        this.valueTextField.setEditable(false);
        this.headersTextField.setText(valueUtils.stringOf(event.getHeaders()));
        this.headersTextField.setEditable(false);
    }
}
