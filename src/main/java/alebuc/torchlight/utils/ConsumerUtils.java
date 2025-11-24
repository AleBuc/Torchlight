package alebuc.torchlight.utils;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.controller.ConsumerController;
import alebuc.torchlight.controller.EventDetailController;
import alebuc.torchlight.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * Scene with topic consumption content.
 */
@Slf4j
public abstract class ConsumerUtils {

    public static final int MAX_ITEMS = 50;

    /**
     * Creates a new stage for the topic consumption.
     *
     * @param topicName topic name
     */
    public static void createConsumer(String topicName, KafkaEventConsumer consumer) {
        ObservableList<Event<?,?>> limitedList = FXCollections.observableArrayList();
        limitedList.addListener((ListChangeListener<Event<?,?>>) change -> {
            if (limitedList.size() > MAX_ITEMS) {
                limitedList.remove(0, limitedList.size() - MAX_ITEMS);
            }
        } );
        ListView<Event<?,?>> eventListView = new ListView<>();
        URL resource = ConsumerUtils.class.getClassLoader().getResource("consumer.fxml");
        ConsumerController controller = new ConsumerController(consumer);
        FXMLLoader consumerW = new FXMLLoader(resource);
        consumerW.setController(controller);
        try {
            Scene scene = new Scene(consumerW.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(topicName);
            stage.show();
            controller.init(topicName);

        } catch (IOException e) {
            log.error("Error during event detail creation.", e);
        }
        Scene scene = new Scene(eventListView, 1000L, 800L);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Consume " + topicName);
        UUID stageId = UUID.randomUUID();
        Service<Void> consumerService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            consumer.processEvents(stageId, topicName, eventListView);
                        } catch (Exception e) {
                            log.error(String.valueOf(e));
                        }
                        return null;
                    }
                };
            }
        };
        eventListView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Event<?,?> event = eventListView.getSelectionModel().getSelectedItem();
                createEventDetailController(event);
            }
        });
        consumerService.start();
        stage.setOnHiding(_ -> {
            consumer.stopTopicConsumption(stageId);
            stage.close();
        });
        stage.show();
    }

    private static void createEventDetailController(Event<?,?> event){
        URL resource = ConsumerUtils.class.getClassLoader().getResource("eventDetail.fxml");
        EventDetailController eventDetailController = new EventDetailController();
        FXMLLoader eventDetail = new FXMLLoader(resource);
        eventDetail.setController(eventDetailController);
        try {
            Scene scene = new Scene(eventDetail.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(String.format("%s-%d-%d", event.getTopic(), event.getPartition(), event.getOffset()));
            stage.show();
            eventDetailController.setTextFields(event);

        } catch (IOException e) {
            log.error("Error during event detail creation.", e);
        }

    }
}
