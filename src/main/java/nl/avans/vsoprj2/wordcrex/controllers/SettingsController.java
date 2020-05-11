package nl.avans.vsoprj2.wordcrex.controllers;

import nl.avans.vsoprj2.wordcrex.Singleton;

public class SettingsController extends Controller {

    public void handleLogoutAction() {
        Singleton.getInstance().setUser(null);
        this.navigateTo("/views/index.fxml");
    }
}
