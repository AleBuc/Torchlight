package alebuc.torchlight.utils;

import alebuc.torchlight.controller.EventDetailController;
import alebuc.torchlight.model.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;

/**
 * Scene with topic consumption content.
 */
@Slf4j
public abstract class ConsumerUtils {

    public static void createEventDetailController(Event<?, ?> event) {
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
