package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BoardTile;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Board;
import nl.avans.vsoprj2.wordcrex.models.Game;
import nl.avans.vsoprj2.wordcrex.models.Tile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class BoardController extends Controller {
    private Game game;
    private Board board;
    private final List<Tile> unconfirmedTiles = new ArrayList<>();

    @FXML
    private GridPane gameGrid;

    private HashMap<Character, Integer> symbolValues;

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
        this.board = new Board(game.getGameId());

        this.symbolValues = this.getSymbolValues();

        this.generateBoard();
        this.loadLetters();
    }

    private void generateBoard() {
        this.gameGrid.getChildren().clear();

        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tile");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int xCord = result.getInt("x") - 1;
                int yCord = result.getInt("y") - 1;
                BoardTile.TileType type = BoardTile.TileType.fromDatabase(result.getString("tile_type"));

                BoardTile boardTile = new BoardTile(type);

                this.gameGrid.add(boardTile, xCord, yCord);
            }

        } catch (SQLException e) {
            this.gameGrid.getChildren().clear();

            throw new DbLoadException(e);
        }
    }

    private void loadLetters() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `woorddeel`, `x-waarden`, `y-waarden` FROM `gelegd` WHERE `game_id` = ?");
            statement.setInt(1, this.game.getGameId());
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String[] letters = result.getString("woorddeel").split(",");
                int[] xCords = Arrays.stream(result.getString("x-waarden").split(",")).mapToInt(Integer::parseInt).toArray();
                int[] yCords = Arrays.stream(result.getString("y-waarden").split(",")).mapToInt(Integer::parseInt).toArray();

                for (int i = 0; i < letters.length; i++) {
                    char letter = letters[i].charAt(0);
                    int xCord = xCords[i] - 1;
                    int yCord = yCords[i] - 1;

                    System.out.print(letter);
                    System.out.print(": ");
                    System.out.print(xCord);
                    System.out.print(",");
                    System.out.print(yCord);

                    BoardTile boardTile = this.getBoardTile(xCord, yCord);

                    boardTile.setConfirmed(true);
                    boardTile.setLetter(letter, this.symbolValues.get(letter));
                }
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public BoardTile getBoardTile(int xCord, int yCord) {
        return (BoardTile) this.gameGrid.getChildren().filtered(node -> GridPane.getRowIndex(node) == xCord).filtered(node -> GridPane.getColumnIndex(node) == yCord).get(0);
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
    private void handleBackButton() {
        this.navigateTo("/views/games.fxml");
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

    private void tilePlaced(Tile placedTile) {
        this.unconfirmedTiles.add(placedTile);

        this.updateLayout();
    }

    private void tileRemoved(Tile placedTile) {
        this.unconfirmedTiles.remove(placedTile);

        this.updateLayout();
    }

    private void updateLayout() {
        List<List<Tile>> words = this.getWords();

        //TODO() Hide point count on layout

        if (this.checkWords(words)) {
            int points = this.calculatePoints(words);

            //TODO() Show point count on layout
        }
    }

    private boolean checkWords(List<List<Tile>> words) {
        // No words
        if (words == null) {
            return false;
        }

        List<String> wordStrings = this.getWordsFromList(words);

        for (String wordString : wordStrings) {
            if (!this.isExistingWord(wordString)) {
                return false;
            }
        }

        return true;
    }

    private List<List<Tile>> getWords() {
        Orientation orientation = this.getWordOrientation();

        List<List<Tile>> words = new ArrayList<>();

        if (orientation == null) {
            return null;
        }

        switch (orientation) {
            case SINGLE_TILE:
                words.add(this.findWord(this.unconfirmedTiles.get(0), true));
                words.add(this.findWord(this.unconfirmedTiles.get(0), false));
                break;
            case VERTICAL:
                int minY = this.unconfirmedTiles.stream().min(Comparator.comparing(Tile::getY)).get().getY();
                int maxY = this.unconfirmedTiles.stream().max(Comparator.comparing(Tile::getY)).get().getY();

                words.add(this.findWord(this.unconfirmedTiles.get(0), false));

                for (int y = minY; y <= maxY; y++) {
                    words.add(this.findWord(this.board.getTile(this.unconfirmedTiles.get(0).getX(), y), true));
                }
                break;
            case HORIZONTAL:
                int minX = this.unconfirmedTiles.stream().min(Comparator.comparing(Tile::getX)).get().getX();
                int maxX = this.unconfirmedTiles.stream().max(Comparator.comparing(Tile::getX)).get().getX();

                words.add(this.findWord(this.unconfirmedTiles.get(0), true));

                for (int x = minX; x <= maxX; x++) {
                    words.add(this.findWord(this.board.getTile(x, this.unconfirmedTiles.get(0).getY()), false));
                }
                break;
        }

        words.removeIf(Objects::isNull);

        return words.size() > 0 ? words : null;
    }

    public int calculatePoints(List<List<Tile>> words) {
        int points = 0;

        for (List<Tile> word : words) {
            int wordPoints = 0;
            int wordMultiplier = 1;

            for (Tile tile : word) {
                int letterMultiplier = 1;
                if (this.unconfirmedTiles.contains(tile)) { // ignores multis if tile was placed on previous turn
                    switch (tile.getTileType()) {
                        case TWOLETTER:
                            letterMultiplier = 2;
                            break;
                        case FOURLETTER:
                            letterMultiplier = 4;
                            break;
                        case SIXLETTER:
                            letterMultiplier = 6;
                            break;
                        case START:
                        case THREEWORD:
                            wordMultiplier *= 3;
                            break;
                        case FOURWORD:
                            wordMultiplier *= 4;
                            break;
                    }
                }

                wordPoints += this.symbolValues.get(tile.getValue()) * letterMultiplier;
            }

            points += (wordPoints * wordMultiplier);
        }

        if (this.unconfirmedTiles.size() == 7 && words.size() != 0) points += 100;
        return points;
    }

    private List<String> getWordsFromList(List<List<Tile>> words) {
        List<String> wordsString = new ArrayList<>();

        for (List<Tile> word : words) {
            wordsString.add(word.stream().map(Tile::getValue).iterator().toString());
        }

        return wordsString;
    }

    private HashMap<Character, Integer> getSymbolValues() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `symbol`, `value` FROM `symbol` WHERE `letterset_code` = ?");
            statement.setString(1, this.game.getLettersetCode());

            ResultSet symbolSet = statement.executeQuery();

            HashMap<Character, Integer> symbolValues = new HashMap<>();

            while (symbolSet.next()) {
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
