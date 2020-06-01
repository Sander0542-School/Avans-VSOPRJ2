package nl.avans.vsoprj2.wordcrex.models;

import javafx.scene.control.Alert;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account extends Model {
    @Column("username")
    private String username;

    public Account(ResultSet resultSet) {
        super(resultSet);
    }

    public String getUsername() {
        return this.username;
    }

    public enum Role {
        PLAYER,
        OBSERVER,
        MODERATOR,
        ADMINISTRATOR
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

    //added override for combobox
    public String toString() {
        return this.getUsername();
    }

    public boolean hasRole(Role role) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement userRolesStatement = connection.prepareStatement("SELECT `role` FROM `accountrole` WHERE `username` = ?");
            userRolesStatement.setString(1, Singleton.getInstance().getUser().getUsername());
            ResultSet roles = userRolesStatement.executeQuery();

            while (roles.next()) {
                if (Account.Role.valueOf(roles.getString("role").toUpperCase()).equals(role)) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            if (WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de rollen.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }

        return false;
    }

    public static Account fromUsername(String username) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM account WHERE username = ?");
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new Account(result);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        return null;
    }

    public static Account fromUsernamePassword(String username, String password) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM account WHERE username = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new Account(result);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        return null;
    }
}
