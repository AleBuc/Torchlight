package alebuc.torchlight.controller;

import alebuc.torchlight.model.Event;
import alebuc.torchlight.utils.ValueUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.Charset;

@RequiredArgsConstructor
public class EventDetailController extends TabPane {

    @FXML
    private TextArea keyTextField;
    @FXML
    private TextArea valueTextField;
    @FXML
    private TextArea headersTextField;

    public void setTextFields(Event<?,?> event) {
        this.keyTextField.setText(ValueUtils.stringOf(event.getKey()));
        this.keyTextField.setEditable(false);
        this.valueTextField.setText(ValueUtils.stringOf(event.getValue()));
        this.valueTextField.setEditable(false);
        this.headersTextField.setText(headersAsString(event.getHeaders()));
        this.headersTextField.setEditable(false);
    }

    private String headersAsString(Headers headers) {
        StringBuilder sb = new StringBuilder();
        for (var header : headers) {
            sb.append(header.key()).append(": ").append(new String(header.value(), Charset.defaultCharset())).append("\n");
        }
        return sb.toString();
    }
}
