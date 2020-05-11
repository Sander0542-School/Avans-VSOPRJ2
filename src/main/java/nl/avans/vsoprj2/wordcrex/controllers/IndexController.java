package nl.avans.vsoprj2.wordcrex.controllers;

public class IndexController extends Controller {

    public void register() {
        this.navigateTo("/views/register.fxml");
    }

    public void login() {
        this.navigateTo("/views/login.fxml");
    }
}
