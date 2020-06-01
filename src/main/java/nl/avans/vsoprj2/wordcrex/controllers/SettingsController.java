package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController extends Controller {

    @FXML
    public Label username;

    @FXML
    public Button btnUserOverview;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.username.setText(Singleton.getInstance().getUser().getUsername());
        if (Singleton.getInstance().getUser().getRole() != null) {
            if (Singleton.getInstance().getUser().userHasRole(Account.Role.ADMINISTRATOR)) {
                this.btnUserOverview.setVisible(true);
            }
        }
    }

    public void handleLogoutAction() {
        Singleton.getInstance().setUser(null);
        this.navigateTo("/views/index.fxml");
    }

    public void handleChangeAccountAction() {
        this.navigateTo("/views/information/changeAccount.fxml");
    }

    public void handleUserOverviewAction() {
        this.navigateTo("/views/information/userOverview.fxml");
    }

    public void handleBackButton() {
        this.navigateTo("/views/games.fxml");
    }
}
