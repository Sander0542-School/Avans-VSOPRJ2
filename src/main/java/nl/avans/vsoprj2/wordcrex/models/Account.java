package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Account extends Model {
    @Column("username")
    private String username;
    @Column("role")
    private String role;

    public Account(ResultSet resultSet) {
        super(resultSet);
    }

    public List<Game> getRequestedGames() {
        final Connection connection = Singleton.getInstance().getConnection();
        final List<Game> result = new ArrayList<>();
        try {
            PreparedStatement requestedGamesStatement = connection.prepareStatement(
                    "SELECT * FROM game " +
                            "WHERE (username_player1 = ? OR username_player2 = ?) AND " +
                            "game_state = 'request' AND answer_player2 = 'unknown'");
            requestedGamesStatement.setString(1, this.getUsername());
            requestedGamesStatement.setString(2, this.getUsername());

            ResultSet resultSet = requestedGamesStatement.executeQuery();

            while (resultSet.next()) {
                result.add(new Game(resultSet));
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public List<Game> getPlayingGames() {
        final Connection connection = Singleton.getInstance().getConnection();
        final List<Game> result = new ArrayList<>();
        try {
            PreparedStatement playingGamesStatement = connection.prepareStatement("SELECT game.*, max(turnplayer1.turn_id) as turnplayer1, max(turnplayer2.turn_id) as turnplayer2 " +
                    "FROM game " +
                    "         LEFT JOIN turnplayer1 on game.game_id = turnplayer1.game_id " +
                    "         LEFT JOIN turnplayer2 on game.game_id = turnplayer2.game_id " +
                    "WHERE (game.username_player1 = ? OR game.username_player2 = ?) AND game.game_state = 'playing' " +
                    "GROUP BY game.game_id;");

            playingGamesStatement.setString(1, this.getUsername());
            playingGamesStatement.setString(2, this.getUsername());

            ResultSet resultSet = playingGamesStatement.executeQuery();

            while (resultSet.next()) {
                result.add(new Game(resultSet));
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public List<Game> getFinishedGames() {
        final Connection connection = Singleton.getInstance().getConnection();
        final List<Game> result = new ArrayList<>();
        try {
            PreparedStatement finishedGamesStatement;

            if (this.getRole().equalsIgnoreCase("observer")) {
                finishedGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE game_state = 'finished' OR game_state = 'resigned'");
            } else {
                finishedGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND (game_state = 'finished' OR game_state = 'resigned')");
                finishedGamesStatement.setString(1, this.getUsername());
                finishedGamesStatement.setString(2, this.getUsername());
            }

            ResultSet resultSet = finishedGamesStatement.executeQuery();

            while (resultSet.next()) {
                result.add(new Game(resultSet));
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public String getUsername() {
        return this.username;
    }

    public String getRole() {
        return this.role;
    }

    public Statistic getStatistic() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            String query = "SELECT (SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` = ?) as games_won," +
                    "(SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` != ? AND (`username_player1` = ? OR `username_player2` = ?) AND `username_winner` IS NOT NULL) as games_lost," +
                    "(SELECT COUNT(`game_id`) FROM `game` WHERE (`username_player1` = ? OR `username_player2` = ?) AND `game_state` = 'finished' AND `username_winner` IS NULL) as games_tied," +
                    "(SELECT COUNT(`game_id`) FROM `game` WHERE `username_winner` != ? AND `game_state` = 'resigned') as games_left," +
                    "(SELECT GREATEST(IFNULL((SELECT MAX(`score1`) FROM `score` WHERE `username_player1` = ? AND `game_state` = 'finished'),0), IFNULL((SELECT MAX(`score2`) FROM `score` WHERE `username_player2` = ? AND `game_state` = 'finished'),0))) as top_game_score," +
                    "(SELECT GREATEST(IFNULL((SELECT MAX(`score`) FROM `turnplayer1` WHERE `username_player1` = ?),0), IFNULL((SELECT MAX(`score`) FROM `turnplayer2` WHERE `username_player2` = ?),0))) as top_word_score;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
                preparedStatement.setString(i, this.getUsername());
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) return new Statistic(resultSet);

        } catch (SQLException ex) {
            throw new DbLoadException(ex);
        }

        return null;
    }

    /**
     * Override for combobox
     *
     * @return the username in string format
     */
    public String toString() {
        return this.getUsername();
    }
}
