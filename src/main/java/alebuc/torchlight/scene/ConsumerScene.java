package alebuc.torchlight.scene;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class ConsumerScene {

    //TODO https://stackoverflow.com/questions/40539310/dependency-injection-and-javafx
    private final KafkaEventConsumer consumer;

    public void createConsumer(String topicName, ListView<String> eventListView) {
        eventListView = new ListView<>();
        Parent root;
        Scene scene = new Scene(eventListView);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Consume " + topicName);
        stage.show();

        ListView<String> finalEventListView = eventListView;
        Service<Void> consumerService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            consumer.processEvents(topicName, finalEventListView);
                        } catch (Exception e) {
                            log.error(Arrays.toString(e.getStackTrace()));
                        }
                        return null;
                    }
                };
            }
        };
        consumerService.start();
    }
}
