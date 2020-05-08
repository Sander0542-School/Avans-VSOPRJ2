package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import nl.avans.vsoprj2.wordcrex.Singleton;

public class SettingsController extends Controller {

    @FXML
    private void handleLogoutAction(MouseEvent event) {
        Singleton.getInstance().setUser(null);
        navigateTo("/views/index.fxml");
    }
}
