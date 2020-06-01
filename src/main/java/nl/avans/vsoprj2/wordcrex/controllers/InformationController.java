package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import nl.avans.vsoprj2.wordcrex.controllers.information.DictionaryController;

public class InformationController extends Controller {
    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/games.fxml");
    }

    @FXML
    private void handleDictionary() {
        this.navigateTo("/views/information/dictionary.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                DictionaryController dictionaryController = new DictionaryController();
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }
}