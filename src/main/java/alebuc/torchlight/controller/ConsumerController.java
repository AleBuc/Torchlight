package alebuc.torchlight.controller;

import alebuc.torchlight.model.consumer.EndChoice;
import alebuc.torchlight.model.consumer.StartChoice;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConsumerController {

    @FXML
    private Label topicName;

    @FXML
    private ListView<?> eventListView;

    @FXML
    private ChoiceBox<StartChoice> startChoice;

    @FXML
    private DatePicker startDateChoice;

    @FXML
    private ChoiceBox<EndChoice> endChoice;

    @FXML
    private DatePicker endDateChoice;

    @FXML
    private Label filteredEventCount;

    @FXML
    private Label totalEventCount;

    @FXML
    private Button consumeButton;

}

