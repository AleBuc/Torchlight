package alebuc.torchlight.scene;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.controller.EventDetailController;
import alebuc.torchlight.model.Event;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * Scene with topic consumption content.
 */
@Slf4j
@RequiredArgsConstructor
public class ConsumerScene {

    private final KafkaEventConsumer consumer;

    /**
     * Creates a new stage for the topic consumption.
     *
     * @param topicName topic name
     */
    public void createConsumer(String topicName) {
        ListView<Event<?,?>> eventListView = new ListView<>();
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

    private void createEventDetailController(Event<?,?> event){
        URL resource = getClass().getClassLoader().getResource("eventDetail.fxml");
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
