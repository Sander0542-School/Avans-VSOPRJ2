package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.Event;
import nl.avans.vsoprj2.wordcrex.controls.navigation.BottomBarItem;

public class StatisticsController extends Controller  {

    public void bottomBarNavigation(Event event) {
        BottomBarItem bottomBarItem = (BottomBarItem) event.getSource();

        if (bottomBarItem.getId().equals("games")) {
            navigateTo("/views/games.fxml");
        }
    }
}
