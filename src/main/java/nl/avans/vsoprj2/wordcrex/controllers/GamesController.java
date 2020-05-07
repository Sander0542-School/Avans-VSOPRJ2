package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.game.BoardController;
import nl.avans.vsoprj2.wordcrex.controls.overview.GameItem;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
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

        gameInvites.managedProperty().bind(gameInvites.visibleProperty());
        gameYours.managedProperty().bind(gameYours.visibleProperty());
        gameTheirs.managedProperty().bind(gameTheirs.visibleProperty());
        finishedGames.managedProperty().bind(finishedGames.visibleProperty());

        loadGames((String) Singleton.getInstance().getUser()); //TODO(getUser() --> getUser().getUsername())
    }

    public void newGamePage() {
        navigateTo("/views/game/new.fxml");

//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            alert.setTitle("Game request");
//            alert.setHeaderText("Je kunt een nieuw spel starten met Sander0542");
//
//        ButtonType buttonTypeDecline = new ButtonType("Decline");
//        ButtonType buttonTypeAccept = new ButtonType("Accept");
//        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
//
//            alert.getButtonTypes().setAll(buttonTypeDecline, buttonTypeAccept, buttonTypeCancel);
//
//        Optional<ButtonType> result = alert.showAndWait();
//            if (result.get() == buttonTypeDecline) {
//            System.out.println("Decline game request");
//        } else if (result.get() == buttonTypeAccept) {
//            System.out.println("Accept game request");
//        } else {
//            // Cancel alert popup
//        }
    }

    private void loadGames(String username) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'request' AND answer_player2 = 'unknown';");
            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            gameInvites.setVisible(false);
            gameInvites.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                GameItem gameItem = new GameItem(new Game(resultSet));
                setGameItemClick(gameItem);

                gameInvites.getChildren().add(gameItem);
                gameInvites.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT game.*, max(turnplayer1.turn_id) as turnplayer1, max(turnplayer2.turn_id) as turnplayer2 " +
                    "FROM game " +
                    "         LEFT JOIN turnplayer1 on game.game_id = turnplayer1.game_id " +
                    "         LEFT JOIN turnplayer2 on game.game_id = turnplayer2.game_id " +
                    "WHERE (game.username_player1 = ? OR game.username_player2 = ?) AND game.game_state = 'playing' " +
                    "GROUP BY game.game_id;");

            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            gameYours.setVisible(false);
            gameYours.getChildren().removeIf(node -> node instanceof GameItem);

            gameTheirs.setVisible(false);
            gameTheirs.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                int turnplayer1 = resultSet.getInt("turnplayer1");
                int turnplayer2 = resultSet.getInt("turnplayer2");

                Game game = new Game(resultSet);
                GameItem gameItem = new GameItem(game);
                setGameItemClick(gameItem);

                if ((turnplayer1 == turnplayer2) ||
                        (game.getUsernamePlayer1().equals(username) && turnplayer1 < turnplayer2) ||
                        (game.getUsernamePlayer2().equals(username) && turnplayer2 < turnplayer1)) {
                    gameYours.getChildren().add(gameItem);
                    gameYours.setVisible(true);
                } else {
                    gameTheirs.getChildren().add(gameItem);
                    gameTheirs.setVisible(true);
                }
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'finished';");
            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            finishedGames.setVisible(false);
            finishedGames.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                GameItem gameItem = new GameItem(new Game(resultSet));
                setGameItemClick(gameItem);

                finishedGames.getChildren().add(gameItem);
                finishedGames.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void setGameItemClick(GameItem gameItem) {
        gameItem.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                navigateTo("/views/game/board", new NavigationListener() {
                    @Override
                    public void beforeNavigate(Controller controller) {
                        BoardController boardController = (BoardController) controller;
                        boardController.setGame(gameItem.getGame());
                    }

                    @Override
                    public void afterNavigate(Controller controller) {

                    }
                });
            }
        });
    }
}
