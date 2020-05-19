package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoardController extends Controller {
    private Game game;

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
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

    private HashMap<Character, Integer> getSymbolValues() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `symbol`, `value` FROM `symbol` WHERE `letterset_code` = ?");
            statement.setString(1, this.game.getLettersetCode());

            ResultSet symbolSet = statement.executeQuery();

            HashMap<Character, Integer> symbolValues = new HashMap<>();

            while(symbolSet.next()) {
                symbolValues.put(symbolSet.getString(1).charAt(0), symbolSet.getInt(2));
            }

            return symbolValues;
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public List<Tile> findWord(Tile tile, boolean horizontal) {
        List<Tile> wordTiles = new ArrayList<>();
        Tile firstLetter = tile;
        int i = 1;

        if (horizontal) {
            while (this.board.hasValue(tile.getX() - i, tile.getY())) {
                firstLetter = this.board.getTile(tile.getX() - i, tile.getY());
                i--;
            }
            wordTiles.add(firstLetter);
            i++;
            while (this.board.hasValue(tile.getX() - i, tile.getY())) {
                wordTiles.add(this.board.getTile(tile.getX() - i, tile.getY()));
                i++;
            }
        } else {
            while (this.board.hasValue(tile.getX(), tile.getY() - i)) {
                firstLetter = this.board.getTile(tile.getX(), tile.getY() - i);
                i--;
            }
            wordTiles.add(firstLetter);
            i++;
            while (this.board.hasValue(tile.getX(), tile.getY() - i)) {
                wordTiles.add(this.board.getTile(tile.getX(), tile.getY() - i));
                i++;
            }
        }

        return wordTiles.size() > 1 ? wordTiles : null;
    }

    private Orientation getWordOrientation() {
        if (this.unconfirmedTiles.size() == 0) {
            return null;
        } else if (this.unconfirmedTiles.size() == 1) {
            return Orientation.SINGLE_TILE;
        }

        Stream<Integer> differentXValues = this.unconfirmedTiles.stream().map(Tile::getX).distinct();
        Stream<Integer> differentYValues = this.unconfirmedTiles.stream().map(Tile::getY).distinct();

        if (differentXValues.count() > 1 && differentYValues.count() > 1) {
            return null;
        }

        if (differentXValues.count() == 1) {
            int minY = this.unconfirmedTiles.stream().min(Comparator.comparing(Tile::getY)).get().getY();
            int maxY = this.unconfirmedTiles.stream().max(Comparator.comparing(Tile::getY)).get().getY();

            for (int y = minY; y <= maxY; y++) {
                if (!this.board.hasValue(differentXValues.findFirst().get(), y)) {
                    return null;
                }
            }

            return Orientation.VERTICAL;
        } else {
            int minX = this.unconfirmedTiles.stream().min(Comparator.comparing(Tile::getX)).get().getX();
            int maxX = this.unconfirmedTiles.stream().max(Comparator.comparing(Tile::getX)).get().getX();

            for (int x = minX; x <= maxX; x++) {
                if (!this.board.hasValue(x, differentYValues.findFirst().get())) {
                    return null;
                }
            }

            return Orientation.HORIZONTAL;
        }
    }

    private enum Orientation {
        SINGLE_TILE,
        HORIZONTAL,
        VERTICAL
    }
}
