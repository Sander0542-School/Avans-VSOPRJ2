package nl.avans.vsoprj2.wordcrex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Hello world!
 */
public class WordCrex extends Application {
    public static final boolean DEBUG_MODE = true;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("WordCrex");
        stage.getIcons().add(new Image("/images/icon.png"));

        Parent sceneBox = new FXMLLoader(getClass().getResource("/views/login.fxml")).load();
        Scene scene = new Scene(sceneBox);

//        if (DEBUG_MODE) {
//            stage.setTitle(String.format("WordCrex - Java: %s - JavaFX: %s", System.getProperty("java.version"), System.getProperty("javafx.version")));
//        }

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
