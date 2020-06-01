package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        if(Singleton.getInstance().getUser().getRole() != null)
        if(this.userIsAdministrator()) {
            this.btnUserOverview.setVisible(true);
        }
    }

    public boolean userIsAdministrator() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement userRolesStatement = connection.prepareStatement("SELECT `role` FROM `accountrole` WHERE `username` = ?");
            userRolesStatement.setString(1, Singleton.getInstance().getUser().getUsername());
            ResultSet roles = userRolesStatement.executeQuery();

            while (roles.next()) {
                if(Account.Role.valueOf(roles.getString("role").toUpperCase()).equals(Account.Role.ADMINISTRATOR)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de rollen.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
            return false;
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
