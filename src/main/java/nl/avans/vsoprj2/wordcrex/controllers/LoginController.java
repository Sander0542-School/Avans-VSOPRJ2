package nl.avans.vsoprj2.wordcrex.controllers;

public class LoginController extends Controller {
    public void handleBackButton() {
        navigateTo("/views/index.fxml");
    }

    public void handleSignInButton() {
        navigateTo("/views/games.fxml");
    }
}
