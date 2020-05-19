package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;

public class SettingsController extends Controller {

    @FXML
    public Label username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.username.setText(Singleton.getInstance().getUser().getUsername());
    }

    public void handleLogoutAction() {
        Singleton.getInstance().setUser(null);
        this.navigateTo("/views/index.fxml");
    }
}
