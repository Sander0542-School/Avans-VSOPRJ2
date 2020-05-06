package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class GamesController extends Controller {
    public void newGamePage() {
        navigateTo("/views/game/new.fxml");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game request");
        alert.setHeaderText("Je kunt een nieuw spel starten met Sander0542");

        ButtonType buttonTypeDecline = new ButtonType("Decline");
        ButtonType buttonTypeAccept = new ButtonType("Accept");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeDecline, buttonTypeAccept, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeDecline) {
            System.out.println("Decline game request");
        } else if (result.get() == buttonTypeAccept) {
            System.out.println("Accept game request");
        } else {
            // Cancel alert popup
        }
    }
}
