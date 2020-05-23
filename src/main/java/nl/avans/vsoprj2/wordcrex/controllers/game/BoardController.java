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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BoardController extends Controller {
    private Game game;
    private final Board board = new Board();

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

        this.symbolValues = this.getSymbolValues();

        this.board.loadLetters(game, this.symbolValues);

        this.loadBoard();
    }

    public List<Tile> getUnconfirmedTiles() {
        List<Tile> tiles = new ArrayList<>();

        for (Tile[] row : this.board.getTiles()) {
            for (Tile tile : row) {
                if (!tile.isConfirmed()) {
                    tiles.add(tile);
                }
            }
        }

        return tiles;
    }

    public void loadBoard() {
        this.gameGrid.getChildren().clear();

        for (int x = 1; x <= Board.BOARD_SIZE; x++) {
            for (int y = 1; y <= Board.BOARD_SIZE; y++) {
                this.gameGrid.add(new BoardTile(this.board.getTile(x, y)), x - 1, y - 1);
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
//        unconfirmedTiles.add(placedTile);

        this.updatePoints();
    }

    private void tileRemoved(Tile placedTile) {
//        unconfirmedTiles.remove(placedTile);

        this.updatePoints();
    }

    private void updatePoints() {
        List<List<Tile>> words = this.getWords();

        //TODO() Hide point count on layout

        if (this.checkWords(words)) {
            Points points = this.calculatePoints(words);

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

        for (Tile tile : this.unconfirmedTiles) { // checking if at least 1 of the new letters touches an older letter
            if (tile.getTileType() == START) return true; // bypass check if this is the first word this game
            if (this.board.getValue(tile.getX() + 1, tile.getY()) != null &&
                    !this.unconfirmedTiles.contains(this.board.getTile(tile.getX() + 1, tile.getY()))) {
                return true;
            }
            if (this.board.getValue(tile.getX(), tile.getY() + 1) != null &&
                    !this.unconfirmedTiles.contains(this.board.getTile(tile.getX(), tile.getY() + 1))) {
                return true;
            }
            if (this.board.getValue(tile.getX() - 1, tile.getY()) != null &&
                    !this.unconfirmedTiles.contains(this.board.getTile(tile.getX() - 1, tile.getY()))) {
                return true;
            }
            if (this.board.getValue(tile.getX(), tile.getY() - 1) != null &&
                    !this.unconfirmedTiles.contains(this.board.getTile(tile.getX(), tile.getY() - 1))) {
                return true;
            }
        }

        return false;
    }

    private List<List<Tile>> getWords() {
        Orientation orientation = this.getWordOrientation();
        List<Tile> unconfirmedTiles = this.getUnconfirmedTiles();

        List<List<Tile>> words = new ArrayList<>();

        Coordinates coordinates;

        if (orientation == null) {
            return null;
        }

        switch (orientation) {
            case SINGLE_TILE:
                words.add(this.findWord(unconfirmedTiles.get(0), true));
                words.add(this.findWord(unconfirmedTiles.get(0), false));
                break;
            case VERTICAL:
                coordinates = this.getCoordinates(unconfirmedTiles);

                words.add(this.findWord(unconfirmedTiles.get(0), false));

                for (int y = coordinates.minY; y <= coordinates.maxY; y++) {
                    words.add(this.findWord(this.board.getTile(coordinates.minX, y), true));
                }
                break;
            case HORIZONTAL:
                coordinates = this.getCoordinates(unconfirmedTiles);

                words.add(this.findWord(unconfirmedTiles.get(0), true));

                for (int x = coordinates.minX; x <= coordinates.maxX; x++) {
                    words.add(this.findWord((this.board.getTile(x, coordinates.minY)), false));
                }
                break;
        }

        words.removeIf(Objects::isNull);

        return words.size() > 0 ? words : null;
    }

    public Coordinates getCoordinates(List<Tile> tiles) {
        Coordinates coordinates = null;

        for (Tile tile : tiles) {
            Board.Coordinate coordinate = this.board.getCoordinate(tile);
            int xCord = coordinate.getX();
            int yCord = coordinate.getY();

            if (coordinates == null) {
                coordinates = new Coordinates(xCord, xCord, yCord, yCord);
            }

            coordinates.minX = Math.min(xCord, coordinates.minX);
            coordinates.maxX = Math.max(xCord, coordinates.maxX);
            coordinates.minY = Math.min(yCord, coordinates.minY);
            coordinates.maxY = Math.max(yCord, coordinates.maxY);
        }

        return coordinates;
    }

    public Points calculatePoints(List<List<Tile>> words) {
        Points points = new Points();

        for (List<Tile> word : words) {
            int wordPoints = 0;
            int wordPointsBonus = 0;
            int wordMultiplier = 1;

            for (Tile tile : word) {
                int letterMultiplier = 1;

                if (!tile.isConfirmed()) {
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

                wordPoints += tile.getWorth();
                wordPointsBonus += tile.getWorth() * letterMultiplier;
            }

            wordPointsBonus = (wordPointsBonus * wordMultiplier) - wordPoints;

            points.addPoints(wordPoints);
            points.addBonus(wordPointsBonus);
        }

        if (this.getUnconfirmedTiles().size() == 7 && words.size() != 0) points.addBonus(100);

        return points;
    }

    public List<String> getWordsFromList(List<List<Tile>> words) {
        List<String> wordsString = new ArrayList<>();

        for (List<Tile> word : words) {
            wordsString.add(word.stream().map(Tile::getLetter).iterator().toString());
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
                symbolValues.put(symbolSet.getString("symbol").charAt(0), symbolSet.getInt("value"));
            }

            return symbolValues;
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public List<Tile> findWord(Tile tile, boolean horizontal) {
        List<Tile> wordTiles = new ArrayList<>();
        Tile firstTile = tile;
        int i = 1;

        Board.Coordinate coordinate = this.board.getCoordinate(tile);
        int xCord = coordinate.getX();
        int yCord = coordinate.getY();

        if (horizontal) {
            while (this.board.getTile(xCord - i, yCord).hasLetter()) {
                firstTile = this.board.getTile(xCord - i, yCord);
                i--;
            }
            wordTiles.add(firstTile);
            i++;
            while (this.board.getTile(xCord - i, yCord).hasLetter()) {
                wordTiles.add(this.board.getTile(xCord - i, yCord));
                i++;
            }
        } else {
            while (this.board.getTile(xCord, yCord - i).hasLetter()) {
                firstTile = this.board.getTile(xCord, yCord - i);
                i--;
            }
            wordTiles.add(firstTile);
            i++;
            while (this.board.getTile(xCord, yCord - i).hasLetter()) {
                wordTiles.add(this.board.getTile(xCord, yCord - i));
                i++;
            }
        }

        return wordTiles.size() > 1 ? wordTiles : null;
    }

    private Orientation getWordOrientation() {
        List<Tile> unconfirmedTiles = this.getUnconfirmedTiles();

        if (unconfirmedTiles.size() == 0) {
            return null;
        } else if (unconfirmedTiles.size() == 1) {
            return Orientation.SINGLE_TILE;
        }

        Coordinates coordinates = this.getCoordinates(unconfirmedTiles);

        Stream<Integer> differentXValues = unconfirmedTiles.stream().map(tile -> this.board.getCoordinate(tile).getX()).distinct();
        Stream<Integer> differentYValues = unconfirmedTiles.stream().map(tile -> this.board.getCoordinate(tile).getY()).distinct();

        if (differentXValues.count() > 1 && differentYValues.count() > 1) {
            return null;
        }

        if (differentXValues.count() == 1) {
            for (int y = coordinates.minY; y <= coordinates.maxY; y++) {
                if (!this.board.getTile(differentXValues.findFirst().get() + 1, y).hasLetter()) {
                    return null;
                }
            }

            return Orientation.VERTICAL;
        } else {
            for (int x = coordinates.minX; x <= coordinates.maxX; x++) {
                if (!this.board.getTile(x, differentYValues.findFirst().get() + 1).hasLetter()) {
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

    private static class Coordinates {
        public int minX;
        public int maxX;

        public int minY;
        public int maxY;

        public Coordinates(int minX, int maxX, int minY, int maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    private static class Points {
        private int points = 0;
        private int bonus = 0;

        public void addPoints(int points) {
            this.points += points;
        }

        public void addBonus(int bonus) {
            this.bonus += bonus;
        }

        public int getPoints() {
            return this.points;
        }

        public int getBonus() {
            return this.bonus;
        }
    }
}
