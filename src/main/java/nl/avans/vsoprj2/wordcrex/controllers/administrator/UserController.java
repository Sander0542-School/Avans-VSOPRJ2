package nl.avans.vsoprj2.wordcrex.controllers.administrator;

import javafx.fxml.FXML;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;

public class UserController extends Controller {
    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }
}