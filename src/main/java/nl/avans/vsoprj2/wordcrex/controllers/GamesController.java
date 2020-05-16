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
import nl.avans.vsoprj2.wordcrex.models.Account;
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

        this.gameInvites.managedProperty().bind(this.gameInvites.visibleProperty());
        this.gameYours.managedProperty().bind(this.gameYours.visibleProperty());
        this.gameTheirs.managedProperty().bind(this.gameTheirs.visibleProperty());
        this.finishedGames.managedProperty().bind(this.finishedGames.visibleProperty());

        this.loadGames(Singleton.getInstance().getUser());
    }

    public void handleNewGameAction() {
        this.navigateTo("/views/game/new.fxml");
    }

    private void gameRequest(GameItem gameItem) {
        Game game = gameItem.getGame();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game request");
        alert.setHeaderText("Je kunt een nieuw spel starten met " + game.getUsernamePlayer2());

        ButtonType buttonTypeDecline = new ButtonType("Decline");
        ButtonType buttonTypeAccept = new ButtonType("Accept");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeDecline, buttonTypeAccept, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonTypeDecline) {
            game.setAnswerPlayer2(Game.Answer.REJECTED);
        } else if (result.get() == buttonTypeAccept) {
            game.setGameState(Game.GameState.PLAYING);
            game.setAnswerPlayer2(Game.Answer.ACCEPTED);
        }

        game.save();
    }

    private void loadGames(Account user) {
        final String username = user.getUsername();
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'request' AND answer_player2 = 'unknown';");
            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            this.gameInvites.setVisible(false);
            this.gameInvites.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                GameItem gameItem = new GameItem(new Game(resultSet));

                if (Singleton.getInstance().getUser().getUsername().equals(gameItem.getGame().getUsernamePlayer1())) {
                    this.setGameItemClick(gameItem);
                } else {
                    gameItem.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            GamesController.this.gameRequest(gameItem);
                        }
                    });
                }

                this.gameInvites.getChildren().add(gameItem);
                this.gameInvites.setVisible(true);
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

            this.gameYours.setVisible(false);
            this.gameYours.getChildren().removeIf(node -> node instanceof GameItem);

            this.gameTheirs.setVisible(false);
            this.gameTheirs.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                int turnplayer1 = resultSet.getInt("turnplayer1");
                int turnplayer2 = resultSet.getInt("turnplayer2");

                Game game = new Game(resultSet);
                GameItem gameItem = new GameItem(game);
                this.setGameItemClick(gameItem);

                if ((turnplayer1 == turnplayer2) ||
                        (game.getUsernamePlayer1().equals(username) && turnplayer1 < turnplayer2) ||
                        (game.getUsernamePlayer2().equals(username) && turnplayer2 < turnplayer1)) {
                    this.gameYours.getChildren().add(gameItem);
                    this.gameYours.setVisible(true);
                } else {
                    this.gameTheirs.getChildren().add(gameItem);
                    this.gameTheirs.setVisible(true);
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

            this.finishedGames.setVisible(false);
            this.finishedGames.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                GameItem gameItem = new GameItem(new Game(resultSet));
                this.setGameItemClick(gameItem);

                this.finishedGames.getChildren().add(gameItem);
                this.finishedGames.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void setGameItemClick(GameItem gameItem) {
        gameItem.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                GamesController.this.navigateTo("/views/game/board.fxml", new NavigationListener() {
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
