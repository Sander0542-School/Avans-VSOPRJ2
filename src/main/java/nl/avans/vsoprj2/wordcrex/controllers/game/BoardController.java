package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BoardTile;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class BoardController extends Controller {
    private Game game;

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
                int xCord = result.getInt("x");
                int yCord = result.getInt("y");
                BoardTile.TileType type = BoardTile.TileType.fromDatabase(result.getString("tile_type"));

                BoardTile boardTile = new BoardTile(type);

                this.gameGrid.add(boardTile, xCord - 1, yCord - 1);
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
                    int xCord = xCords[i];
                    int yCord = yCords[i];

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
        return (BoardTile) this.gameGrid.getChildren().filtered(node -> GridPane.getColumnIndex(node) == xCord - 1).filtered(node -> GridPane.getRowIndex(node) == yCord - 1).get(0);
    }

    public List<BoardTile> getUnconfirmedTiles() {
        List<BoardTile> boardTiles = new ArrayList<>();

        this.gameGrid.getChildren().forEach(node -> {
            BoardTile boardTile = (BoardTile) node;

            if (boardTile.isConfirmed()) {
                boardTiles.add(boardTile);
            }
        });

        return boardTiles;
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

    private void tilePlaced(BoardTile placedTile) {
//        unconfirmedTiles.add(placedTile);

        this.updatePoints();
    }

    private void tileRemoved(BoardTile placedTile) {
//        unconfirmedTiles.remove(placedTile);

        this.updatePoints();
    }

    private void updatePoints() {
        List<List<BoardTile>> words = this.getWords();

        //TODO() Hide point count on layout

        if (this.checkWords(words)) {
            Points points = this.calculatePoints(words);

            //TODO() Show point count on layout
        }
    }

    private boolean checkWords(List<List<BoardTile>> words) {
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

    private List<List<BoardTile>> getWords() {
        Orientation orientation = this.getWordOrientation();

        List<BoardTile> unconfirmedTiles = this.getUnconfirmedTiles();
        List<List<BoardTile>> words = new ArrayList<>();

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
                    words.add(this.findWord(this.getBoardTile(coordinates.minX, y), true));
                }
                break;
            case HORIZONTAL:
                coordinates = this.getCoordinates(unconfirmedTiles);

                words.add(this.findWord(unconfirmedTiles.get(0), true));

                for (int x = coordinates.minX; x <= coordinates.maxX; x++) {
                    words.add(this.findWord((this.getBoardTile(x, coordinates.minY)), false));
                }
                break;
        }

        words.removeIf(Objects::isNull);

        return words.size() > 0 ? words : null;
    }

    private Coordinates getCoordinates(List<BoardTile> boardTiles) {
        Coordinates coordinates = null;

        for (BoardTile boardTile : boardTiles) {
            int xCord = GridPane.getRowIndex(boardTile) - 1;
            int yCord = GridPane.getColumnIndex(boardTile) - 1;

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

    public Points calculatePoints(List<List<BoardTile>> words) {
        Points points = new Points();

        for (List<BoardTile> word : words) {
            int wordPoints = 0;
            int wordPointsBonus = 0;
            int wordMultiplier = 1;

            for (BoardTile tile : word) {
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

    private List<String> getWordsFromList(List<List<BoardTile>> words) {
        List<String> wordsString = new ArrayList<>();

        for (List<BoardTile> word : words) {
            wordsString.add(word.stream().map(BoardTile::getLetter).iterator().toString());
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

    public List<BoardTile> findWord(BoardTile tile, boolean horizontal) {
        List<BoardTile> wordTiles = new ArrayList<>();
        BoardTile firstLetter = tile;
        int i = 1;

        int xCord = GridPane.getColumnIndex(tile) + 1;
        int yCord = GridPane.getRowIndex(tile) + 1;

        if (horizontal) {
            while (this.getBoardTile(xCord - i, yCord).hasLetter()) {
                firstLetter = this.getBoardTile(xCord - i, yCord);
                i--;
            }
            wordTiles.add(firstLetter);
            i++;
            while (this.getBoardTile(xCord - i, yCord).hasLetter()) {
                wordTiles.add(this.getBoardTile(xCord - i, yCord));
                i++;
            }
        } else {
            while (this.getBoardTile(xCord, yCord - i).hasLetter()) {
                firstLetter = this.getBoardTile(xCord, yCord - i);
                i--;
            }
            wordTiles.add(firstLetter);
            i++;
            while (this.getBoardTile(xCord, yCord - i).hasLetter()) {
                wordTiles.add(this.getBoardTile(xCord, yCord - i));
                i++;
            }
        }

        return wordTiles.size() > 1 ? wordTiles : null;
    }

    private Orientation getWordOrientation() {
        List<BoardTile> unconfirmedTiles = this.getUnconfirmedTiles();

        if (unconfirmedTiles.size() == 0) {
            return null;
        } else if (unconfirmedTiles.size() == 1) {
            return Orientation.SINGLE_TILE;
        }

        Coordinates coordinates = this.getCoordinates(unconfirmedTiles);

        Stream<Integer> differentXValues = unconfirmedTiles.stream().map(GridPane::getColumnIndex).distinct();
        Stream<Integer> differentYValues = unconfirmedTiles.stream().map(GridPane::getRowIndex).distinct();

        if (differentXValues.count() > 1 && differentYValues.count() > 1) {
            return null;
        }

        if (differentXValues.count() == 1) {
            for (int y = coordinates.minY; y <= coordinates.maxY; y++) {
                if (!this.getBoardTile(differentXValues.findFirst().get() + 1, y).hasLetter()) {
                    return null;
                }
            }

            return Orientation.VERTICAL;
        } else {
            for (int x = coordinates.minX; x <= coordinates.maxX; x++) {
                if (!this.getBoardTile(x, differentYValues.findFirst().get() + 1).hasLetter()) {
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
