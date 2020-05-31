package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;

import javax.xml.transform.Result;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AccountController extends Controller {
    @FXML
    private Label errorLabel;

    @FXML
    private PasswordField oldPassword;

    @FXML
    private PasswordField newPassword;

    @FXML
    private PasswordField confirmNewPassword;

    @FXML
    private Label username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.username.setText(Singleton.getInstance().getUser().getUsername());
    }

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }

    @FXML
    private void handleChangePasswordAction() {
        this.errorLabel.setVisible(false);
        if(this.oldPassword.getText().trim().isEmpty() || this.newPassword.getText().trim().isEmpty() || this.confirmNewPassword.getText().trim().isEmpty()) {
            this.showErrorMessage("Niet alle velden zijn ingevuld!");
            return;
        } else if(!this.newPassword.getText().equals(this.confirmNewPassword.getText())) {
            this.showErrorMessage("Het nieuwe wachtwoord komt niet overeen met het herhalende wachtwoord!");
            return;
        }

        try {
            Connection connection = Singleton.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT `password` FROM `account` WHERE `username` = ?");
            statement.setString(1, Singleton.getInstance().getUser().getUsername());
            ResultSet oldPassword = statement.executeQuery();

            if(oldPassword.next()) {
                if(!this.oldPassword.getText().equals(oldPassword.getString(1))) {
                    this.showErrorMessage("Het oude wachtwoord is niet correct!");
                    return;
                }
            }

            PreparedStatement newPasswordStatement = connection.prepareStatement("UPDATE `account` SET `password` = ? WHERE `username` = ?");
            newPasswordStatement.setString(1, this.newPassword.getText().trim());
            newPasswordStatement.setString(2, Singleton.getInstance().getUser().getUsername());
            newPasswordStatement.executeUpdate();

            this.oldPassword.clear();
            this.newPassword.clear();
            this.confirmNewPassword.clear();

            Alert invalidWordDialog = new Alert(Alert.AlertType.INFORMATION, "Je wachtwoord is succesvol geüpdate!");
            invalidWordDialog.setTitle("Succes");
            invalidWordDialog.showAndWait();
        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan tijdens het wijzigen van je wachtwoord.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }

    private void showErrorMessage(String message) {
        this.errorLabel.setText(message);
        this.errorLabel.setVisible(true);
    }
}
