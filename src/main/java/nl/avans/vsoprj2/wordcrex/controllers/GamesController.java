package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.game.BoardController;
import nl.avans.vsoprj2.wordcrex.controls.navigation.BottomBarItem;
import nl.avans.vsoprj2.wordcrex.controls.overview.GameItem;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class GamesController extends Controller {

    @FXML
    private VBox gameInvites;
    @FXML
    private VBox gameYours;
    @FXML
    private VBox gameTheirs;
    @FXML
    private VBox finishedGames;

    private ResultSet requestedGamesResult;
    private ResultSet playingGamesResult;
    private ResultSet finishedGamesResult;
    private final Timer autoFetch = new Timer();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.gameInvites.managedProperty().bind(this.gameInvites.visibleProperty());
        this.gameYours.managedProperty().bind(this.gameYours.visibleProperty());
        this.gameTheirs.managedProperty().bind(this.gameTheirs.visibleProperty());
        this.finishedGames.managedProperty().bind(this.finishedGames.visibleProperty());

        this.loadGames(Singleton.getInstance().getUser());
        this.renderGames();

        this.autoFetch.scheduleAtFixedRate(this.createTimerTask(), 5000, 5000);
    }

    public void handleNewGameAction() {
        this.autoFetch.cancel();
        this.autoFetch.purge();
        this.navigateTo("/views/game/new.fxml");
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (WordCrex.DEBUG_MODE) System.out.println("GamesController: Autofetch running.");
                GamesController.this.loadGames(Singleton.getInstance().getUser());
                if (WordCrex.DEBUG_MODE) System.out.println("GamesController: Autofetch data updated rendering.");
                Platform.runLater(GamesController.this::renderGames);
            }
        };
    }

    private void gameRequest(GameItem gameItem) {
        Game game = gameItem.getGame();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game request");
        alert.setHeaderText("Je kunt een nieuw spel starten met " + game.getUsernamePlayer1());

        ButtonType buttonTypeDecline = new ButtonType("Weigeren", ButtonBar.ButtonData.NO);
        ButtonType buttonTypeAccept = new ButtonType("Accepteren", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeCancel = new ButtonType("Annuleren", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeAccept, buttonTypeDecline, buttonTypeCancel);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.NO) {
                game.setAnswerPlayer2(Game.Answer.REJECTED);
                game.save();
            } else if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                game.setGameState(Game.GameState.PLAYING);
                game.setAnswerPlayer2(Game.Answer.ACCEPTED);
                game.save();
            }

            this.loadGames(Singleton.getInstance().getUser());
            this.renderGames();
        });
    }

    private void renderGames() {
        if (this.requestedGamesResult == null || this.playingGamesResult == null || this.finishedGamesResult == null)
            return;
        final String currentUsername = Singleton.getInstance().getUser().getUsername();
        try {
            //region Render requested games
            this.gameInvites.setVisible(false);
            this.gameInvites.getChildren().removeIf(node -> node instanceof GameItem);

            while (this.requestedGamesResult.next()) {
                GameItem gameItem = new GameItem(new Game(this.requestedGamesResult));

                if (Singleton.getInstance().getUser().getUsername().equals(gameItem.getGame().getUsernamePlayer1())) {
                    this.setGameItemClick(gameItem);
                } else {
                    gameItem.setOnMouseClicked(event -> GamesController.this.gameRequest(gameItem));
                }

                this.gameInvites.getChildren().add(gameItem);
                this.gameInvites.setVisible(true);
            }
            //endregion

            //region Render playing games
            this.gameYours.setVisible(false);
            this.gameYours.getChildren().removeIf(node -> node instanceof GameItem);

            this.gameTheirs.setVisible(false);
            this.gameTheirs.getChildren().removeIf(node -> node instanceof GameItem);

            while (this.playingGamesResult.next()) {
                int turnplayer1 = this.playingGamesResult.getInt("turnplayer1");
                int turnplayer2 = this.playingGamesResult.getInt("turnplayer2");

                Game game = new Game(this.playingGamesResult);
                GameItem gameItem = new GameItem(game);
                this.setGameItemClick(gameItem);

                if ((turnplayer1 == turnplayer2) ||
                        (game.getUsernamePlayer1().equals(currentUsername) && turnplayer1 < turnplayer2) ||
                        (game.getUsernamePlayer2().equals(currentUsername) && turnplayer2 < turnplayer1)) {
                    this.gameYours.getChildren().add(gameItem);
                    this.gameYours.setVisible(true);
                } else {
                    this.gameTheirs.getChildren().add(gameItem);
                    this.gameTheirs.setVisible(true);
                }
            }
            //endregion

            //region Render finished games
            this.finishedGames.setVisible(false);
            this.finishedGames.getChildren().removeIf(node -> node instanceof GameItem);

            while (this.finishedGamesResult.next()) {
                GameItem gameItem = new GameItem(new Game(this.finishedGamesResult));
                this.setGameItemClick(gameItem);

                this.finishedGames.getChildren().add(gameItem);
                this.finishedGames.setVisible(true);
            }
            //endregion
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void loadGames(Account user) {
        final String username = user.getUsername();
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement requestedGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'request' AND answer_player2 = 'unknown';");
            requestedGamesStatement.setString(1, username);
            requestedGamesStatement.setString(2, username);

            this.requestedGamesResult = requestedGamesStatement.executeQuery();

            PreparedStatement playingGamesStatement = connection.prepareStatement("SELECT game.*, max(turnplayer1.turn_id) as turnplayer1, max(turnplayer2.turn_id) as turnplayer2 " +
                    "FROM game " +
                    "         LEFT JOIN turnplayer1 on game.game_id = turnplayer1.game_id " +
                    "         LEFT JOIN turnplayer2 on game.game_id = turnplayer2.game_id " +
                    "WHERE (game.username_player1 = ? OR game.username_player2 = ?) AND game.game_state = 'playing' " +
                    "GROUP BY game.game_id;");

            playingGamesStatement.setString(1, username);
            playingGamesStatement.setString(2, username);

            this.playingGamesResult = playingGamesStatement.executeQuery();

            PreparedStatement finishedGamesStatement = connection.prepareStatement("SELECT * FROM game WHERE (username_player1 = ? OR username_player2 = ?) AND game_state = 'finished';");
            finishedGamesStatement.setString(1, username);
            finishedGamesStatement.setString(2, username);

            this.finishedGamesResult = finishedGamesStatement.executeQuery();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void setGameItemClick(GameItem gameItem) {
        gameItem.setOnMouseClicked(event -> {
            if (gameItem.getGame().getGameState() != Game.GameState.PLAYING) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Wachten op tegenstander");
                alert.setHeaderText(String.format("Je tegenstander %s heeft je uitnodiging nog niet geaccepteerd.", gameItem.getGame().getUsernamePlayer2()));
                alert.showAndWait();
                return;
            }

            this.autoFetch.cancel();
            this.autoFetch.purge();

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
        );
    }

    public void handleBottomBarNavigation(Event event) {
        BottomBarItem bottomBarItem = (BottomBarItem) event.getSource();

        if (bottomBarItem.getId().equals("statistics")) {
            this.autoFetch.cancel();
            this.autoFetch.purge();
            this.navigateTo("/views/statistics.fxml");
        }
    }

    public void handleOptionsMenu(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();

        this.autoFetch.cancel();
        this.autoFetch.purge();

        switch (menuItem.getId()) {
            case "info":
                this.navigateTo("/views/information.fxml");
                break;
            case "settings":
                this.navigateTo("/views/settings.fxml");
                break;
        }
    }
}
