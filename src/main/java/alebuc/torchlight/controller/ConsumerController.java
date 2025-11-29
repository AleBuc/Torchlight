package alebuc.torchlight.controller;

import alebuc.torchlight.component.TimeSpinner;
import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.model.Event;
import alebuc.torchlight.model.consumer.EndChoice;
import alebuc.torchlight.model.consumer.StartChoice;
import alebuc.torchlight.utils.ConsumerUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import static alebuc.torchlight.utils.ConsumerUtils.createEventDetailController;

@Slf4j
@RequiredArgsConstructor
public class ConsumerController extends GridPane {

    private final KafkaEventConsumer consumer;
    private static final int MAX_ITEMS = 50;

    @FXML
    private Label topicName;

    @FXML
    @Setter
    private ListView<Event<?, ?>> eventListView;

    private final ObservableList<Event<?, ?>> limitedList = FXCollections.observableArrayList();

    @FXML
    private ChoiceBox<StartChoice> startChoice;

    @FXML
    private DatePicker startDateChoice;
    @FXML
    private TimeSpinner startTimeChoice;

    @FXML
    private ChoiceBox<EndChoice> endChoice;

    @FXML
    private DatePicker endDateChoice;
    @FXML
    private TimeSpinner endTimeChoice;

    @FXML
    private Label filteredEventCount;

    @FXML
    private Label totalEventCount;

    @FXML
    private Button consumeButton;

    private Service<Void> consumerService;
    private UUID stageId;

    public ConsumerController(String topicName, KafkaEventConsumer consumer) {
        this.consumer = consumer;

        limitedList.addListener((ListChangeListener<Event<?, ?>>) change -> {
            if (limitedList.size() > MAX_ITEMS) {
                limitedList.remove(0, limitedList.size() - MAX_ITEMS);
            }
        });

        URL resource = ConsumerUtils.class.getClassLoader().getResource("consumer.fxml");
        FXMLLoader consumerW = new FXMLLoader(resource);
        consumerW.setController(this);
        Stage stage = new Stage();
        try {
            Scene scene = new Scene(consumerW.load());
            eventListView.setItems(limitedList);
            eventListView.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    Event<?, ?> event = eventListView.getSelectionModel().getSelectedItem();
                    createEventDetailController(event);
                }
            });

            stage.setScene(scene);
            stage.setTitle("Consume " + topicName);
            stage.show();
            this.init(topicName);

        } catch (IOException e) {
            log.error("Error during event detail creation.", e);
        }

        stage.setOnHiding(_ -> {
            consumer.stopTopicConsumption(stageId);
            stage.close();
        });
        stage.show();
    }

    private void init(String topicName) {
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
        this.startTimeChoice.setVisible(StartChoice.DEFAULT.isShowDatePicker());
        this.endDateChoice.setVisible(StartChoice.DEFAULT.isShowDatePicker());
        this.endTimeChoice.setVisible(StartChoice.DEFAULT.isShowDatePicker());

        this.startChoice.setOnAction(_ -> {
            this.startDateChoice.setVisible(startChoice.getValue().isShowDatePicker());
            this.startTimeChoice.setVisible(startChoice.getValue().isShowDatePicker());
        });
        this.endChoice.setOnAction(_ -> {
            this.endDateChoice.setVisible(endChoice.getValue().isShowDatePicker());
            this.endTimeChoice.setVisible(endChoice.getValue().isShowDatePicker());
        });
        totalEventCount.textProperty().bind(Bindings.size(limitedList).asString("%d events in topic"));
    }

    @FXML
    void consumeButtonAction() {
        if (consumerService == null || !consumerService.isRunning()) {
            eventListView.getItems().clear();
            startConsumption();
        } else if (consumerService.isRunning()) {
            consumer.stopTopicConsumption(stageId);
            consumeButton.setText("Consume");
        }
    }

    private void startConsumption() {
        stageId = UUID.randomUUID();
        totalEventCount.setVisible(true);
        consumerService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            consumer.processEvents(stageId, topicName.getText(), limitedList);
                        } catch (Exception e) {
                            log.error(String.valueOf(e));
                        }
                        return null;
                    }
                };
            }
        };
        consumerService.start();
        consumeButton.setText("Stop");
    }

}

