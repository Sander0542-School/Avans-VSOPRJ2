package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;

public class PanelController extends Controller {
    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/information.fxml");
    }
}
