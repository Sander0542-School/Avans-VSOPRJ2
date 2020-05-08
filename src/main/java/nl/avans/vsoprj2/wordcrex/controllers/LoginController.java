package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbConnectionException;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController extends Controller {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label error;

    public void backButton() {
        navigateTo("/views/index.fxml");
    }

    public void handleLoginAction(MouseEvent event) {
        error.setVisible(false);

        if (!username.getText().trim().isEmpty() && !password.getText().trim().isEmpty()) {
            Connection connection = Singleton.getInstance().getConnection();
            try {
                PreparedStatement statement;
                statement = connection.prepareStatement("SELECT a.username, ar.role FROM account a INNER JOIN accountrole ar ON a.username = ar.username WHERE a.username=? && a.password=?");
                statement.setString(1, username.getText());
                statement.setString(2, password.getText());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    Account account = new Account(result);
                    Singleton.getInstance().setUser(account);
                    navigateTo("/views/games.fxml");
                } else {
                    this.showIncorrectAuthError();
                }

            } catch (SQLException e) {
                throw new DbConnectionException(e);
            }
        } else {
            this.showIncorrectAuthError();
        }
    }

    private void showIncorrectAuthError() {
        error.setText("Inloggen mislukt, foute gebruikersnaam of wachtwoord.");
        error.setVisible(true);
    }
}
