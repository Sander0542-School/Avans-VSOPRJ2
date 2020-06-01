package nl.avans.vsoprj2.wordcrex;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        URL resource = this.getClass().getResource("/views/index.fxml");

        if (DEBUG_MODE) {
            try {
                PreparedStatement statement = Singleton.getInstance().getConnection().prepareStatement("SELECT a.username, ar.role FROM account a INNER JOIN accountrole ar ON a.username = ar.username WHERE a.username=?");
                statement.setString(1, "test-admin");
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    Account account = new Account(result);
                    Singleton.getInstance().setUser(account);
                    resource = this.getClass().getResource("/views/games.fxml");
                }
            } catch (SQLException e) {
                throw new DbLoadException(e);
            }
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