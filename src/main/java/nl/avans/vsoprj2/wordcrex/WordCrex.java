package nl.avans.vsoprj2.wordcrex;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;

public class WordCrex extends Application {
    public static final boolean DEBUG_MODE = true;

    public static void handleException(Throwable e) {
        if (WordCrex.DEBUG_MODE) throw new RuntimeException(e);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("WordCrex");
        stage.getIcons().add(new Image("/images/icon.png"));

        URL resource = this.getClass().getResource("/views/index.fxml");

        if (DEBUG_MODE) {
            Account account = Account.fromUsername("jagermeester");
            Singleton.getInstance().setUser(account);
            resource = this.getClass().getResource("/views/games.fxml");
        }

        Parent parent = new FXMLLoader(resource).load();
        Scene scene = new Scene(parent);

        stage.setScene(scene);

        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }
}
