package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BoardController extends Controller {
    private Game game;

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * @param winner - Account model
     */
    public void endGame(Account winner) {
        this.game.setGameState(Game.GameState.FINISHED);
        this.game.setWinner(winner);

        this.game.save();
    }

    public void passGameClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game passen");
        alert.setHeaderText("Weet je zeker dat je wil passen?");

        ButtonType buttonTypeCancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeOk = new ButtonType("OK");

        alert.getButtonTypes().setAll(buttonTypeCancel, buttonTypeOk);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonTypeOk) {
//            this.passGame();
        }
    }

    private void passGame() {
        Connection connection = Singleton.getInstance().getConnection();
        String currentUsername = Singleton.getInstance().getUser().getUsername();
        boolean isPlayer1 = this.game.getUsernamePlayer1().equals(Singleton.getInstance().getUser().getUsername());

        //TODO Check Turn status
        //SELECT (SELECT turnaction_type FROM turnplayer1 WHERE game_id = 546 ORDER BY turn_id DESC) AS type_player1, (SELECT turnaction_type FROM turnplayer2 WHERE game_id = 546 ORDER BY turn_id DESC) AS type_player2

        try {
            if (isPlayer1) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO turnplayer1(game_id, turn_id, username_player1, bonus, score, turnaction_type) VALUES (?, (SELECT (IFnull(MAX(turn_id), 0) + 1) AS next_turn FROM `turnplayer1` WHERE game_id = ?), ?, 0, 0, 'pass')");
                statement.setInt(1, this.game.getGameId());
                statement.setInt(2, this.game.getGameId());
                statement.setString(3, currentUsername);

                statement.execute();
            } else {
            }


        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void giveNewLetterInHand() {
        //TODO Give new letters
        //TODO Start new turn
    }

    public boolean isExistingWord(String word) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'accepted');");
            statement.setString(1, word);
            statement.setString(2, this.game.getLettersetCode());
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    @FXML
    private void handleScoreboardAction() {
        this.navigateTo("/views/game/scoreboard.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                ScoreboardController scoreboardController = (ScoreboardController) controller;
                scoreboardController.setGame(BoardController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }

    @FXML
    private void handleChatAction() {
        this.navigateTo("/views/game/chat.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                ChatController chatController = (ChatController) controller;
                chatController.setGame(BoardController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }
}
