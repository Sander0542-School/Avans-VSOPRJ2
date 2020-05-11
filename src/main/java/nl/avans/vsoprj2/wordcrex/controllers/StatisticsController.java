package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticsController extends Controller {
    @FXML
    private Label name;
    @FXML
    private Label gamesWonLabel;
    @FXML
    private Label gamesLostLabel;
    @FXML
    private Label gamesTiedLabel;
    @FXML
    private Label gamesLeftLabel;
    @FXML
    private Label topGameScoreLabel;
    @FXML
    private Label topWordScoreLabel;

    private String gamesWon;
    private String gamesLost;
    private String gamesTied;
    private String gamesLeft;
    private String topGameScore;
    private String topWordScore;

    public void initialize() {
        setUser();
        setStatistics();
    }

    private void setUser() {
        name.setText(Singleton.getInstance().getUser().getUsername());
    }

    /**
     * Gathering all the statistics from a player.
     */
    private void setStatistics() {
        Connection connection = Singleton.getInstance().getConnection();
        Account account = Singleton.getInstance().getUser();

        try {
            String query = "SELECT (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` = ?) as games_won," +
                    " (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` != ? AND `username_player1` = ? OR `username_player2` = ? AND `username_winner` IS NOT NULL) as games_lost," +
                    " (SELECT COUNT(`game_id`) FROM `game` WHERE `username_player1` = ? OR `username_player2` = ? AND `game_state` = 'finished' AND `username_winner` IS NULL) as games_tied," +
                    " (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` != ? AND `game_state` = 'resigned') as games_left," +
                    " (SELECT GREATEST(IFNULL((SELECT MAX(`score1`) FROM `score` WHERE `username_player1` = ? AND `game_state` = 'finished'),0), IFNULL((SELECT MAX(`score2`) FROM `score` WHERE `username_player2` = ? AND `game_state` = 'finished'),0))) as top_game_score," +
                    " (SELECT GREATEST(IFNULL((SELECT MAX(`score` + `bonus`) FROM `turnplayer1` WHERE `username_player1` = ?),0), IFNULL((SELECT MAX(`score` + `bonus`) FROM `turnplayer2` WHERE `username_player2` = ?),0))) as top_word_score;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
                preparedStatement.setString(i, account.getUsername());
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                gamesWon.setText(resultSet.getString("games_won"));
                gamesLost.setText(resultSet.getString("games_lost"));
                gamesTied.setText(resultSet.getString("games_tied"));
                gamesLeft.setText(resultSet.getString("games_left"));
                topGameScore.setText(resultSet.getString("top_game_score"));
                topWordScore.setText(resultSet.getString("top_word_score"));
            }
        } catch (SQLException ex) {
            throw new DbLoadException(ex);
        }
    }
}
