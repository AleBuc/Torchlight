package alebuc.torchlight.controller;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.model.consumer.EndChoice;
import alebuc.torchlight.model.consumer.StartChoice;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConsumerController extends GridPane {

    private final KafkaEventConsumer consumer;

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

    public void init(String topicName) {
        this.topicName.setText(topicName);

        this.startChoice.getItems().addAll(StartChoice.values());
        this.endChoice.getItems().addAll(EndChoice.values());

        this.startChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(StartChoice choice) {
                return choice == null ? "" : choice.getName();
            }

            @Override
            public StartChoice fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (StartChoice c : StartChoice.values()) {
                    if (c.getName().equals(string)) {
                        return c;
                    }
                }
                return null;
            }
        });

        this.endChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(EndChoice choice) {
                return choice == null ? "" : choice.getName();
            }

            @Override
            public EndChoice fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (EndChoice c : EndChoice.values()) {
                    if (c.getName().equals(string)) {
                        return c;
                    }
                }
                return null;
            }
        });

        this.startChoice.setValue(StartChoice.DEFAULT);
        this.endChoice.setValue(EndChoice.DEFAULT);

        this.startDateChoice.setVisible(StartChoice.DEFAULT.isShowDatePicker());
        this.endDateChoice.setVisible(StartChoice.DEFAULT.isShowDatePicker());

        this.startChoice.setOnAction(_ -> this.startDateChoice.setVisible(startChoice.getValue().isShowDatePicker()));
        this.endChoice.setOnAction(_ -> this.endDateChoice.setVisible(endChoice.getValue().isShowDatePicker()));
    }

}

