package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account extends Model {
    @Column("username")
    private String username;
    @Column("role")
    private String role;

    public Account(ResultSet resultSet) {
        super(resultSet);
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
}
