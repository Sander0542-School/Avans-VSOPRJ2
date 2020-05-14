package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.models.Game;
import nl.avans.vsoprj2.wordcrex.models.ScoreboardRound;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ScoreboardController extends Controller {
    private Game game;
    private List<ScoreboardRound> scoreboardRounds = new ArrayList<>();

    @FXML
    private VBox roundRowContainer;

    /**
     * Sets the game id and fetches + renders it.
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - The game from the database to display.
     */
    public void setGame(Game game) {
        if (game == null) throw new IllegalArgumentException("Game may not be null");
        this.game = game;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void backToGameScreen() {
        this.navigateTo(String.valueOf(this.getClass().getResource("/views/game/board.fxml")), new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                BoardController boardController = (BoardController) controller;
                boardController.setGame(ScoreboardController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }
}
