package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.Event;

public class LoginController extends Controller {
    public void backButton(Event event) {
        navigateTo("/views/index.fxml");
    }
}
