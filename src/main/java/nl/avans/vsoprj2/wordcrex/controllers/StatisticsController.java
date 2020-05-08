package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.MenuItem;
import nl.avans.vsoprj2.wordcrex.controls.navigation.BottomBarItem;

public class StatisticsController extends Controller {

    public void bottomBarNavigation(Event event) {
        BottomBarItem bottomBarItem = (BottomBarItem) event.getSource();

        if (bottomBarItem.getId().equals("games")) {
            navigateTo("/views/games.fxml");
        }
    }

    public void handleOptionsMenu(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();

        switch (menuItem.getId()) {
            case "info":
                navigateTo("/views/information.fxml");
                break;
            case "settings":
                navigateTo("/views/settings.fxml");
                break;
        }
    }
}
