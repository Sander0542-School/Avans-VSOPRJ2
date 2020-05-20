package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;

public class InformationController extends Controller {
    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/games.fxml");
    }
}
