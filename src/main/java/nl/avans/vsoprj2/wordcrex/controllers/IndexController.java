package nl.avans.vsoprj2.wordcrex.controllers;

public class IndexController extends Controller {

    public void register() {
        navigateTo("/views/register.fxml");
    }

    public void login() {
        navigateTo("/views/login.fxml");
    }
}
