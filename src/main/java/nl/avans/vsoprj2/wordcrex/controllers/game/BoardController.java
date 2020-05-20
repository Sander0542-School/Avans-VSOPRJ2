package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BackgroundTile;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.LetterTile;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Board;
import nl.avans.vsoprj2.wordcrex.models.Game;
import nl.avans.vsoprj2.wordcrex.models.Tile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoardController extends Controller {
    private Game game;
    private Board board;

    @FXML
    private GridPane gameGrid;



    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
        System.out.println(game.getGameId());
        this.board = new Board(game.getGameId());
        this.updateView();
    }

    private void updateView(){
        Tile[][] grid = this.board.getGrid();
        for(int x = 0; x < grid.length; x++){
            for(int y = 0; y < grid.length; y++){
                Character value = grid[x][y].getValue();


                if(value != null){
                    this.gameGrid.add(new LetterTile(value, 1), x, y);
                }else
                {
                    Board.TileType tileType = grid[x][y].getTileType();
                    this.gameGrid.add(new BackgroundTile(tileType), x, y);
                }
            }
        }
    }

    /**
     * @param winner - Account model
     */
    public void endGame(Account winner) {
        this.game.setGameState(Game.GameState.FINISHED);
        this.game.setWinner(winner);

        this.game.save();
    }

    public boolean isExistingWord(String word) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'accepted');");
            statement.setString(1, word);
            statement.setString(2, this.game.getLettersetCode());
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    @FXML
    private void handleScoreboardAction() {
        this.navigateTo("/views/game/scoreboard.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                ScoreboardController scoreboardController = (ScoreboardController) controller;
                scoreboardController.setGame(BoardController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }

    @FXML
    private void handleChatAction() {
        this.navigateTo("/views/game/chat.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                ChatController chatController = (ChatController) controller;
                chatController.setGame(BoardController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }
}
