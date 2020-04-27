package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Controller {

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

        Controller controller = loader.getController();

        if (navigationListener != null) {
            navigationListener.beforeNavigate(controller);
        }

        try {
            Parent parent = loader.load();
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
        public void beforeNavigate(Controller controller);

        public void afterNavigate(Controller controller);
    }
}
