package nl.avans.vsoprj2.wordcrex.controllers;

public class IndexController extends Controller {

    public void handleRegisterClick() {
        this.navigateTo("/views/register.fxml");
    }

    public void handleLoginClick() {
        this.navigateTo("/views/login.fxml");
    }
}
