package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class Controller implements Initializable {

    @FXML
    private BorderPane borderPane;

    public Stage getStage() {
        return (Stage) borderPane.getScene().getWindow();
    }

    public void navigateTo(String resource) {
        navigateTo(resource, null);
    }

    public void navigateTo(String resource, NavigationListener navigationListener) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));

        try {
            Parent parent = loader.load();
            Controller controller = loader.getController();

            if (navigationListener != null) {
                navigationListener.beforeNavigate(controller);
            }

            Scene scene = new Scene(parent);
            this.getStage().setScene(scene);

            if (navigationListener != null) {
                navigationListener.afterNavigate(controller);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface NavigationListener {
        void beforeNavigate(Controller controller);

        void afterNavigate(Controller controller);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
