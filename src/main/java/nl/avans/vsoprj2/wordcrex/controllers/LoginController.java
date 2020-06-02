package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.models.Account;

public class LoginController extends Controller {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label error;

    public void handleBackButton() {
        this.navigateTo("/views/index.fxml");
    }

    public void handleLoginAction() {
        this.error.setVisible(false);

        if (!this.username.getText().trim().isEmpty() && !this.password.getText().trim().isEmpty()) {
            Account account = Account.fromUsernamePassword(this.username.getText(), this.password.getText());
          
            if (account != null) {
                Singleton.getInstance().setUser(account);
                this.navigateTo("/views/games.fxml");
            } else {
                this.showIncorrectAuthError();
            }
          
        } else {
            this.showIncorrectAuthError();
        }
    }

    private void showIncorrectAuthError() {
        this.error.setText("Inloggen mislukt, foute gebruikersnaam of wachtwoord.");
        this.error.setVisible(true);
    }

    @FXML
    private void handleEnterReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) this.handleLoginAction();
    }
}
