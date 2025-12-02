package alebuc.torchlight.controller;

import alebuc.torchlight.component.TimeSpinner;
import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.model.Event;
import alebuc.torchlight.model.EventFilter;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * Constructor for the ConsumerController class, responsible for initializing a consumer
     * interface for a specified Kafka topic. This method sets up the necessary UI components
     * and behavior for consuming and displaying Kafka events in a JavaFX application.
     *
     * @param topicName the name of the Kafka topic to be consumed
     * @param consumer  the KafkaEventConsumer responsible for managing the consumption of events
     */
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

    /**
     * Initializes the user interface elements and sets up the default values and behaviors for
     * start and end choices, including their converters, visibility toggles, and bindings.
     *
     * @param topicName The name of the topic to be displayed and used in the consumer interface.
     */
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

    /**
     * Handles the action triggered by the consume button in the UI.
     * <p>
     * This method initiates or stops the event consumption process based on the current state of the consumer service.
     * If the service is not initialized or currently stopped, the method clears the event list view and starts
     * the event consumption process. Conversely, if the consumer service is already running, it stops the
     * topic consumption for the provided stage identifier and updates the consume button's text accordingly.
     * Additionally, this method toggles the filters in the UI to enable or disable user inputs related
     * to event filtering.
     * <p>
     * Preconditions:
     * - The consumer service should have been properly initialized for successful event consumption.
     * - UI components used (e.g., eventListView, consumeButton) should be correctly linked via FXML.
     * <p>
     * Postconditions:
     * - The event list view is cleared when consumption is initiated.
     * - The consumption state is updated based on user actions, modifying the button's text and enabling
     * or disabling filters as appropriate.
     */
    @FXML
    void consumeButtonAction() {
        if (consumerService == null || !consumerService.isRunning()) {
            eventListView.getItems().clear();
            startConsumption();
        } else if (consumerService.isRunning()) {
            consumer.stopTopicConsumption(stageId);
            consumeButton.setText("Consume");
        }
        toggleFilters();
    }

    /**
     * Initiates the consumption of events by setting up necessary configurations,
     * building an event filter, and starting the consumer service.
     * <p>
     * This method generates a unique identifier for the current stage of consumption
     * and updates the visibility of the total event count. It constructs an event
     * filter based on the user-provided configuration, including start and end
     * choices, as well as optional date and time values.
     * <p>
     * A service task is created to process events using the configured event filter
     * and other parameters such as stage ID, topic name, and limited list data.
     * The service is then started, and the consume button's text is updated to
     * indicate that the consumption process can be stopped.
     * <p>
     * The method handles potential exceptions during event processing by logging
     * errors.
     */
    private void startConsumption() {
        stageId = UUID.randomUUID();
        totalEventCount.setVisible(true);
        EventFilter.EventFilterBuilder filterBuilder = EventFilter.builder();
        filterBuilder.startChoice(startChoice.getValue());
        filterBuilder.endChoice(endChoice.getValue());
        if (startDateChoice != null && startDateChoice.getValue() != null) {
            LocalDateTime startLocalDateTime = startDateChoice.getValue()
                    .atTime(startTimeChoice != null ? startTimeChoice.getValue() : LocalTime.MIDNIGHT);
            filterBuilder.startLocalDateTime(startLocalDateTime);
        }
        if (endDateChoice != null && endDateChoice.getValue() != null) {
            LocalDateTime endLocalDateTime = endDateChoice.getValue()
                    .atTime(endTimeChoice != null ? endTimeChoice.getValue() : LocalTime.MIDNIGHT);
            filterBuilder.endLocalDateTime(endLocalDateTime);
        }

        consumerService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            consumer.processEvents(stageId, topicName.getText(), limitedList, filterBuilder.build());
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

    /**
     * Toggles the enabled/disabled state of start and end filter UI components.
     * <p>
     * This method checks the current state of the `startChoice` component
     * (enabled or disabled). It then switches its state and also updates the
     * state of related components (`startDateChoice`, `startTimeChoice`,
     * `endChoice`, `endDateChoice`, `endTimeChoice`) to match the new state.
     */
    private void toggleFilters() {
        boolean currentlyDisabled = startChoice.isDisabled();
        startChoice.setDisable(!currentlyDisabled);
        startDateChoice.setDisable(!currentlyDisabled);
        startTimeChoice.setDisable(!currentlyDisabled);

        endChoice.setDisable(!currentlyDisabled);
        endDateChoice.setDisable(!currentlyDisabled);
        endTimeChoice.setDisable(!currentlyDisabled);
    }

}

