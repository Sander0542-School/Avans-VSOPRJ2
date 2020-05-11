package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.models.Statistic;

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

    public void initialize() {
        this.statistic = getStatistic();
        render();
    }

    /**
     * Rendering the given information to the stage.
     */
    private void render() {
        this.name.setText(Singleton.getInstance().getUser().getUsername());

        this.gamesWon.setText(statistic.getGamesWon());
        this.gamesLost.setText(statistic.getGamesLost());
        this.gamesTied.setText(statistic.getGamesTied());
        this.gamesLeft.setText(statistic.getGamesLeft());
        this.topGameScore.setText(statistic.getTopGameScore());
        this.topWordScore.setText(statistic.getTopWordScore());
    }

    /**
     * Gathering the statistics from a player.
     */
    private Statistic getStatistic() {
        return Singleton.getInstance().getUser().getStatistic();
    }
}
