package nl.avans.vsoprj2.wordcrex.controllers.administrator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UserController extends Controller {
    @FXML
    public ComboBox userComboBox;

    @FXML
    public ComboBox userRoleComboBox;

    @FXML
    public Label currentUser;

    @FXML
    public Button changeUserRoleButton;

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.getAllUsers();
        this.getAllRoles();
    }

    private void getAllRoles() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement usersStatement = connection.prepareStatement("SELECT `role` FROM `role`");
            ResultSet users = usersStatement.executeQuery();

            while (users.next()) {
                this.userRoleComboBox.getItems().add(users.getString("role"));
            }

        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de rollen.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }

    private void getAllUsers() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement usersStatement = connection.prepareStatement("SELECT a.username, ar.role FROM account a INNER JOIN accountrole ar ON a.username = ar.username ");
            ResultSet users = usersStatement.executeQuery();

            while (users.next()) {
                if(!users.getString("username").equals(Singleton.getInstance().getUser().getUsername())) {
                    Account account = new Account(users);
                    this.userComboBox.getItems().add(account);
                }
            }

        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de gebruikers.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }

    @FXML
    private void handleUserSelection() {
        this.currentUser.setText(((Account) this.userComboBox.getValue()).getUsername());
        this.userRoleComboBox.getSelectionModel().select(((Account) this.userComboBox.getValue()).getRole());
        this.currentUser.setVisible(true);
        this.userRoleComboBox.setVisible(true);
        this.changeUserRoleButton.setVisible(true);
    }

    @FXML
    private void handleUserRoleChangeAction() {
        System.out.println(this.userRoleComboBox.getValue());
    }
}