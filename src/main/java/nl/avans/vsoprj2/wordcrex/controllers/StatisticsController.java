package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticsController extends Controller {
    @FXML
    Label name;

    @FXML
    Label gamesWon;

    @FXML
    Label gamesLost;

    @FXML
    Label gamesTied;

    @FXML
    Label gamesLeft;

    @FXML
    Label topGameScore;

    @FXML
    Label topWordScore;

    private User user;
    Connection connection = null;

    public void initialize() {
        this.connection = Singleton.getInstance().getConnection();

        // TEMP USER UNTILL USER MODEL IS AVAIL
        user = new User();
        user.setUsername("Lidewij");
        user.setPassword("rrr");

        setUser();
        setStatistics();
    }

    private void setUser() {
        name.setText(user.getUsername());
    }

    private void setStatistics() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String query = null;

        try {
            query = "SELECT (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` = ?) as games_won," +
                    " (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` != ? AND `username_player1` = ? OR `username_player2` = ? AND `username_winner` IS NOT NULL) as games_lost," +
                    " (SELECT COUNT(`game_id`) FROM `game` WHERE `username_player1` = ? OR `username_player2` = ? AND `game_state` = 'finished' AND `username_winner` IS NULL) as games_tied," +
                    " (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` != ? AND `game_state` = 'resigned') as games_left," +
                    " (SELECT GREATEST(IFNULL((SELECT MAX(`score1`) FROM `score` WHERE `username_player1` = ? AND `game_state` = 'finished'),0), IFNULL((SELECT MAX(`score2`) FROM `score` WHERE `username_player2` = ? AND `game_state` = 'finished'),0))) as top_game_score;";
            stmt = connection.prepareStatement(query);

            for (int i = 1; i <= stmt.getParameterMetaData().getParameterCount(); i++) {
                stmt.setString(i, user.getUsername());
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                gamesWon.setText(rs.getString("games_won"));
                gamesLost.setText(rs.getString("games_lost"));
                gamesTied.setText(rs.getString("games_tied"));
                gamesLeft.setText(rs.getString("games_left"));
                topGameScore.setText(rs.getString("top_game_score"));
                topWordScore.setText("0" /*rs.getString("top_game_score")*/);
            }

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                stmt = null;
            }
        }

    }


}
