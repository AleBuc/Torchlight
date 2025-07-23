package alebuc.torchlight;

import alebuc.torchlight.configuration.AppConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.IOException;

/**
 * JavaFX App
 */
public class JavaFXApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ui.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Torchlight");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        Platform.exit();
    }
}