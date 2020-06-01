package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.game.BoardController;
import nl.avans.vsoprj2.wordcrex.controls.navigation.BottomBarItem;
import nl.avans.vsoprj2.wordcrex.controls.overview.GameItem;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ObserverController extends Controller {

    private final Timer autoFetch = new Timer();
    @FXML
    private VBox playingGames;
    @FXML
    private VBox finishedGames;
    private List<Game> playingGamesResult;
    private List<Game> finishedGamesResult;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.playingGames.managedProperty().bind(this.playingGames.visibleProperty());
        this.finishedGames.managedProperty().bind(this.finishedGames.visibleProperty());

        this.loadGames(Singleton.getInstance().getUser());
        this.renderGames();

        this.autoFetch.scheduleAtFixedRate(this.createTimerTask(), 5000, 5000);
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (WordCrex.DEBUG_MODE) System.out.println("GamesController: Autofetch running.");
                ObserverController.this.loadGames(Singleton.getInstance().getUser());
                if (WordCrex.DEBUG_MODE) System.out.println("GamesController: Autofetch data updated rendering.");
                Platform.runLater(ObserverController.this::renderGames);
            }
        };
    }

    private void renderGames() {
        if (this.playingGamesResult == null || this.finishedGamesResult == null) return;

        //region Render playing games
        this.playingGames.setVisible(false);
        this.playingGames.getChildren().removeIf(node -> node instanceof GameItem);

        for (Game game : this.playingGamesResult) {
            GameItem gameItem = new GameItem(game);
            this.setGameItemClick(gameItem);

            this.playingGames.getChildren().add(gameItem);
        }
        this.playingGames.setVisible(true);
        //endregion

        //region Render finished games
        this.finishedGames.setVisible(false);
        this.finishedGames.getChildren().removeIf(node -> node instanceof GameItem);

        for (Game game : this.finishedGamesResult) {
            GameItem gameItem = new GameItem(game);
            this.setGameItemClick(gameItem);

            this.finishedGames.getChildren().add(gameItem);
        }
        this.finishedGames.setVisible(true);
        //endregion
    }

    private void loadGames(Account account) {
        this.playingGamesResult = account.getPlayingGames(true);
        this.finishedGamesResult = account.getFinishedGames(true);
    }

    private void setGameItemClick(GameItem gameItem) {
        gameItem.setOnMouseClicked(event -> {
            this.autoFetch.cancel();
            this.autoFetch.purge();

            ObserverController.this.navigateTo("/views/game/board.fxml", new NavigationListener() {
                @Override
                public void beforeNavigate(Controller controller) {
                    BoardController boardController = (BoardController) controller;
                    boardController.setGame(gameItem.getGame());
                }

                @Override
                public void afterNavigate(Controller controller) {

                }
            });
        });
    }

    public void handleBottomBarNavigation(Event event) {
        BottomBarItem bottomBarItem = (BottomBarItem) event.getSource();

        switch (bottomBarItem.getId().toLowerCase()) {
            case "statistics":
                this.autoFetch.cancel();
                this.autoFetch.purge();
                this.navigateTo("/views/statistics.fxml");
                break;
            case "games":
                this.autoFetch.cancel();
                this.autoFetch.purge();
                this.navigateTo("/views/games.fxml");
                break;
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
            case "instellingen":
                this.navigateTo("/views/settings.fxml");
                break;
        }
    }
}
