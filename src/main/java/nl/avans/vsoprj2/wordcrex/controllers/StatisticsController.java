package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controls.navigation.BottomBarItem;
import nl.avans.vsoprj2.wordcrex.models.Statistic;

import java.net.URL;
import java.util.ResourceBundle;

public class StatisticsController extends Controller {
    @FXML
    private Label name;
    @FXML
    private Label gamesWon;
    @FXML
    private Label gamesLost;
    @FXML
    private Label gamesTied;
    @FXML
    private Label gamesLeft;
    @FXML
    private Label topGameScore;
    @FXML
    private Label topWordScore;

    private Statistic statistic;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.statistic = this.getStatistic();
        this.render();
    }

    /**
     * Rendering the given information to the stage.
     */
    private void render() {
        this.name.setText(Singleton.getInstance().getUser().getUsername());

        this.gamesWon.setText(this.statistic.getGamesWon().toString());
        this.gamesLost.setText(this.statistic.getGamesLost().toString());
        this.gamesTied.setText(this.statistic.getGamesTied().toString());
        this.gamesLeft.setText(this.statistic.getGamesLeft().toString());
        this.topGameScore.setText(this.statistic.getTopGameScore().toString());
        this.topWordScore.setText(this.statistic.getTopWordScore().toString());
    }

    public void handleBottomBarNavigation(Event event) {
        BottomBarItem bottomBarItem = (BottomBarItem) event.getSource();

        switch (bottomBarItem.getId().toLowerCase()) {
            case "observer":
                this.navigateTo("/views/observer.fxml");
                break;
            case "games":
                this.navigateTo("/views/games.fxml");
                break;
        }
    }

    public void handleOptionsMenu(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();

        switch (menuItem.getId()) {
            case "info":
                this.navigateTo("/views/information.fxml");
                break;
            case "instellingen":
                this.navigateTo("/views/settings.fxml");
                break;
        }
    }

    /**
     * Gathering the statistics from a player.
     */
    private Statistic getStatistic() {
        return Singleton.getInstance().getUser().getStatistic();
    }
}
