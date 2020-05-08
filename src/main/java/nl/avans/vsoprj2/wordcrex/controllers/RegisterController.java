package nl.avans.vsoprj2.wordcrex.controllers;
public class RegisterController extends Controller {
    public void handleBackButton() {
        navigateTo("/views/index.fxml");
    }

    public void handleSignUpButton() {
        navigateTo("/views/games.fxml");
    }
}
