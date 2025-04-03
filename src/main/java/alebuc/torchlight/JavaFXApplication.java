package alebuc.torchlight;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * JavaFX App
 */
public class JavaFXApplication extends Application {

    private ListView<String> eventListView;
    private final Logger log = LoggerFactory.getLogger(JavaFXApplication.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        eventListView = new ListView<>();
        Scene scene = new Scene(eventListView, 600, 600);
        stage.setTitle("Torchlight");
        stage.setScene(scene);
        stage.show();

        KafkaEventConsumer consumer = new KafkaEventConsumer();

        Service<Void> consumerService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            consumer.processEvents(eventListView);
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

    @Override
    public void stop() {
        Platform.exit();
    }
}