package nl.avans.vsoprj2.wordcrex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Hello world!
 */
public class WordCrex extends Application {
    public static final boolean DEBUG_MODE = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("WordCrex");
        stage.getIcons().add(new Image("/images/icon.png"));

        Parent parent = new FXMLLoader(getClass().getResource("/views/index.fxml")).load();
        Scene scene = new Scene(parent);

//        if (DEBUG_MODE) {
//            stage.setTitle(String.format("WordCrex - Java: %s - JavaFX: %s", System.getProperty("java.version"), System.getProperty("javafx.version")));
//        }

        stage.setScene(scene);

        stage.show();
    }
}
