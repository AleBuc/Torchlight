package alebuc.torchlight;

import alebuc.torchlight.model.Event;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

/**
 * JavaFX App
 */
public class JavaFXApplication extends Application {

    private TableView<Event<String, String>> eventTableView;
    private final Logger log = LoggerFactory.getLogger(JavaFXApplication.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        eventTableView = new TableView<>();
        TableColumn<Event<String, String>, Instant> timestampColumn = new TableColumn<>("Publish time");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        TableColumn<Event<String, String>, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn<Event<String, String>, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        eventTableView.getColumns().add(timestampColumn);
        eventTableView.getColumns().add(keyColumn);
        eventTableView.getColumns().add(valueColumn);
        eventTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        root.setCenter(eventTableView);

        Scene scene = new Scene(root, 600, 600);
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
                            consumer.processEvents(eventTableView);
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