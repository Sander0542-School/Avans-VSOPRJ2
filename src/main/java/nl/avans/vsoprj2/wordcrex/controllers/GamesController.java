package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controls.overview.GameItem;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GamesController extends Controller {

    @FXML
    private VBox gameInvites;
    @FXML
    private VBox gameYours;
    @FXML
    private VBox gameTheirs;
    @FXML
    private VBox finishedGames;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        loadGames((String) Singleton.getInstance().getUser()); //TODO(getUser() --> getUser().getUsername())
    }

    private void loadGames(String username) {
        Connection connection = Singleton.getInstance().getConnection();
        Object user = Singleton.getInstance().getUser();
        PreparedStatement statement;

        try {
            statement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'request' AND answer_player2 = 'unknown';");
            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            gameInvites.setManaged(false);
            gameInvites.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                GameItem gameItem = new GameItem(new Game(resultSet));
                gameInvites.getChildren().add(gameItem);

                gameInvites.setManaged(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        try {
            statement = connection.prepareStatement("SELECT game.*, max(turnplayer1.turn_id) as turnplayer1, max(turnplayer2.turn_id) as turnplayer2 " +
                    "FROM game " +
                    "         LEFT JOIN turnplayer1 on game.game_id = turnplayer1.game_id " +
                    "         LEFT JOIN turnplayer2 on game.game_id = turnplayer2.game_id " +
                    "WHERE (game.username_player1 = ? OR game.username_player2 = ?) AND game.game_state = 'playing' " +
                    "GROUP BY game.game_id;");

            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            gameYours.setManaged(false);
            gameYours.getChildren().removeIf(node -> node instanceof GameItem);

            gameTheirs.setManaged(false);
            gameTheirs.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                int turnplayer1 = resultSet.getInt("turnplayer1");
                int turnplayer2 = resultSet.getInt("turnplayer2");

                Game game = new Game(resultSet);
                GameItem gameItem = new GameItem(game);

                if ((turnplayer1 == turnplayer2) ||
                        (game.getUsernamePlayer1() == user && turnplayer1 < turnplayer2) || //TODO(user --> user.getUsername())
                        (game.getUsernamePlayer2() == user && turnplayer2 < turnplayer1)) { //TODO(user --> user.getUsername())
                    gameYours.getChildren().add(gameItem);
                    gameYours.setManaged(true);
                } else {
                    gameTheirs.getChildren().add(gameItem);
                    gameTheirs.setManaged(true);
                }
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        try {
            statement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'finished';");
            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            finishedGames.setManaged(false);
            finishedGames.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                GameItem gameItem = new GameItem(new Game(resultSet));
                finishedGames.getChildren().add(gameItem);

                finishedGames.setManaged(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
