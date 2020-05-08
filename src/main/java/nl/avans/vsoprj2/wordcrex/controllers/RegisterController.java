package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import nl.avans.vsoprj2.wordcrex.Singleton;
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


    public void handleRegisterAction(MouseEvent event) {
        error.setVisible(false);

        if (username.getText().trim().isEmpty() || password.getText().trim().isEmpty() || repeatpassword.getText().trim().isEmpty()) {
            this.showErrorMessage("Niet alle velden zijn ingevuld.");
            return;
        } else {
            if (!password.getText().equals(repeatpassword.getText())) {
                this.showErrorMessage("De wachtwoorden komen niet overeen.");
                return;
            }
        }

        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement("SELECT username FROM account where username=?");
            statement.setString(1, username.getText());
            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                connection.setAutoCommit(false);

                PreparedStatement insertaccount;
                insertaccount = connection.prepareStatement("INSERT INTO account (username, password) VALUES(?, ?)");
                insertaccount.setString(1, username.getText());
                insertaccount.setString(2, password.getText());

                PreparedStatement insertrole;
                insertrole = connection.prepareStatement("INSERT INTO accountrole (username, role) VALUES(?, 'player')");
                insertrole.setString(1, username.getText());

                int insertaccountresult = insertaccount.executeUpdate();
                int insertroleresult = insertrole.executeUpdate();

                if (insertaccountresult > 0 && insertroleresult > 0) {
                    connection.commit();
                    navigateTo("/views/login.fxml");
                } else {
                    connection.rollback();
                    this.showErrorMessage("Het account kon niet worden aangemaakt.");
                }
            } else {
                this.showErrorMessage("Gebruikersnaam '" + username.getText() + "' bestaat al.");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                this.showErrorMessage("Het account kon niet worden aangemaakt.");
            } catch (SQLException e2) {
                throw new DbLoadException(e2);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e3) {
                throw new DbLoadException(e3);
            }
        }
    }

    private void showErrorMessage(String message) {
        error.setText(message);
        error.setVisible(true);
    }

}
