package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import nl.avans.vsoprj2.wordcrex.Singleton;

public class SettingsController extends Controller {

    @FXML
    protected void handleLogoutAction(ActionEvent event) {
        Singleton.getInstance().setUser(null);
        navigateTo("/views/index.fxml");
    }
}
