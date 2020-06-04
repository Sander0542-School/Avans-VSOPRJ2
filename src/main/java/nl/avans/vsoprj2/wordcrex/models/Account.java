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
import java.util.ArrayList;
import java.util.List;

public class Account extends Model {
    @Column("username")
    private String username;

    public Account(ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Fetches all the playing games you have access to
     *
     * @return list of the games that should be rendered in the playing games container
     */
    public List<Game> getPlayingGames() {
        return this.getPlayingGames(false);
    }

    /**
     * Fetches all the game requests you have access to
     *
     * @return list of the games that should be rendered in the requested games container
     */
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
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de aangevraagde spellen.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();

            return new ArrayList<>();
        }
    }

    /**
     * Fetches all the finished games you have access to
     *
     * @return list of the games that should be rendered in the finished games container
     */
    public List<Game> getFinishedGames() {
        return this.getFinishedGames(false);
    }

    /**
     * Fetches all the playing games you have access to
     *
     * @param observable if this is true the method will also return observable games
     *
     * @return list of the games
     */
    public List<Game> getPlayingGames(final boolean observable) {
        final Connection connection = Singleton.getInstance().getConnection();
        final List<Game> result = new ArrayList<>();
        try {
            PreparedStatement playingGamesStatement;

            if (this.hasRole(Role.OBSERVER) && observable) {
                playingGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE " +
                        "game.username_player1 != ? AND game.username_player2 != ? AND game.game_state = 'playing'");
            } else {
                playingGamesStatement = connection.prepareStatement("SELECT * FROM game " +
                        "WHERE (game.username_player1 = ? OR game.username_player2 = ?) AND game.game_state = 'playing'");

            }

            playingGamesStatement.setString(1, this.getUsername());
            playingGamesStatement.setString(2, this.getUsername());

            ResultSet resultSet = playingGamesStatement.executeQuery();

            while (resultSet.next()) {
                result.add(new Game(resultSet));
            }
            return result;
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de lopende spellen.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();

            return new ArrayList<>();
        }
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

    /**
     * Fetches all the finished games you have access to
     *
     * @param observable if this is true the method will also return observable games
     *
     * @return list of games
     */
    public List<Game> getFinishedGames(final boolean observable) {
        final Connection connection = Singleton.getInstance().getConnection();
        final List<Game> result = new ArrayList<>();
        try {
            PreparedStatement finishedGamesStatement;

            if (this.hasRole(Role.OBSERVER) && observable) {
                finishedGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE username_player1 != ? " +
                        "AND username_player2 != ? AND (game_state = 'finished' OR game_state = 'resigned')");
            } else {
                finishedGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND (game_state = 'finished' OR game_state = 'resigned')");
            }

            finishedGamesStatement.setString(1, this.getUsername());
            finishedGamesStatement.setString(2, this.getUsername());

            ResultSet resultSet = finishedGamesStatement.executeQuery();

            while (resultSet.next()) {
                result.add(new Game(resultSet));
            }
            return result;
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de afgelopen spellen.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();

            return new ArrayList<>();
        }
    }

    /**
     * Override for combobox
     *
     * @return the username in string format
     */
    public String toString() {
        return this.getUsername();
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

        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van je statistieken.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
        }

        return null;
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
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van je rollen.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
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
            WordCrex.handleException(e);

            return null;
        }

        return null;
    }
}
