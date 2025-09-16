package alebuc.torchlight.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventDetailController extends TabPane {

    @FXML
    private TextArea headersTextField;

    @FXML
    private TextArea keyTextField;

    @FXML
    private TextArea valueTextField;
}
