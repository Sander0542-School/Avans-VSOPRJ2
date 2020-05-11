package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
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

        this.gamesWon.setText(this.statistic.getGamesWon());
        this.gamesLost.setText(this.statistic.getGamesLost());
        this.gamesTied.setText(this.statistic.getGamesTied());
        this.gamesLeft.setText(this.statistic.getGamesLeft());
        this.topGameScore.setText(this.statistic.getTopGameScore());
        this.topWordScore.setText(this.statistic.getTopWordScore());
    }

    /**
     * Gathering the statistics from a player.
     */
    private Statistic getStatistic() {
        return Singleton.getInstance().getUser().getStatistic();
    }
}
