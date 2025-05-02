package alebuc.torchlight;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

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
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getClassLoader().getResource("ui.fxml"))));
        eventListView = new ListView<>();
        Scene scene = new Scene(root);
        stage.setTitle("Torchlight");
        stage.setScene(scene);
        stage.show();

//        KafkaEventConsumer consumer = new KafkaEventConsumer();

//        Service<Void> consumerService = new Service<>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<>() {
//                    @Override
//                    protected Void call() {
//                        try {
//                            consumer.getTopics();
//                        } catch (Exception e) {
//                            log.error(Arrays.toString(e.getStackTrace()));
//                        }
//                        return null;
//                    }
//                };
//            }
//        };
//        consumerService.start();


    }

    @Override
    public void stop() {
        Platform.exit();
    }
}