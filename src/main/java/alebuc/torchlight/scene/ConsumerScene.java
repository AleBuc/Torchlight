package alebuc.torchlight.scene;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        ListView<String> eventListView = new ListView<>();
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
        consumerService.start();
        stage.setOnHiding(_ -> {
            consumer.stopTopicConsumption(stageId);
            stage.close();
        });
        stage.show();
    }
}
