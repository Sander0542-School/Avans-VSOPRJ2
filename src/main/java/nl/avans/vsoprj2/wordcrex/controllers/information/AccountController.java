package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;

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

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }

    @FXML
    private void handleChangePasswordAction() {

    }

    @FXML
    private void handleEnterReleased() {

    }
}
