package nl.avans.vsoprj2.wordcrex.controllers;

public class IndexController extends Controller {

    public void handleRegisterClick() {
        navigateTo("/views/register.fxml");
    }

    public void handleLoginClick() {
        navigateTo("/views/login.fxml");
    }
}
