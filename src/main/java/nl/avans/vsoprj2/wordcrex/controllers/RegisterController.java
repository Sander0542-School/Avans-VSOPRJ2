package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController extends Controller {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField repeatpassword;
    @FXML
    private Label error;

    public void handleRegisterAction() {
        this.error.setVisible(false);

        if (this.username.getText().trim().isEmpty() || this.password.getText().trim().isEmpty() || this.repeatpassword.getText().trim().isEmpty()) {
            this.showErrorMessage("Niet alle velden zijn ingevuld.");
            return;
        } else {
            if (!this.password.getText().equals(this.repeatpassword.getText())) {
                this.showErrorMessage("De wachtwoorden komen niet overeen.");
                return;
            }
        }

        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM account where username=?");
            statement.setString(1, this.username.getText());
            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                connection.setAutoCommit(false);

                PreparedStatement insertaccount = connection.prepareStatement("INSERT INTO account (username, password) VALUES(?, ?)");
                insertaccount.setString(1, this.username.getText());
                insertaccount.setString(2, this.password.getText());

                PreparedStatement insertrole = connection.prepareStatement("INSERT INTO accountrole (username, role) VALUES(?, 'player')");
                insertrole.setString(1, this.username.getText());

                int insertaccountresult = insertaccount.executeUpdate();
                int insertroleresult = insertrole.executeUpdate();

                if (insertaccountresult > 0 && insertroleresult > 0) {
                    connection.commit();
                    this.navigateTo("/views/login.fxml");
                } else {
                    connection.rollback();
                    this.showErrorMessage("Het account kon niet worden aangemaakt.");
                }
            } else {
                this.showErrorMessage("Gebruikersnaam '" + this.username.getText() + "' bestaat al.");
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            try {
                connection.rollback();
                this.showErrorMessage("Het account kon niet worden aangemaakt.");
            } catch (SQLException e2) {
                throw new DbLoadException(e2);
            }
            throw new DbLoadException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e3) {
                throw new DbLoadException(e3);
            }
        }
    }

    public void handleBackButton() {
        this.navigateTo("/views/index.fxml");
    }

    private void showErrorMessage(String message) {
        this.error.setText(message);
        this.error.setVisible(true);
    }

}
