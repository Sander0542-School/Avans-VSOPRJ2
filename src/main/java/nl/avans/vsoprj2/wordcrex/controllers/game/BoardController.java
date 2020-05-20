package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BackgroundTile;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.LetterTile;
import nl.avans.vsoprj2.wordcrex.controls.games.SuggestedAccount;
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

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
        this.board = new Board(game.getGameId());
        this.updateView();
    }

    private void updateView() {
        Tile[][] grid = this.board.getGrid();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                Character value = grid[x][y].getValue();

                if (value != null) {
                    this.gameGrid.add(new LetterTile(value, 1), x, y);
                } else {
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

    public void passGameClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game passen");
        alert.setHeaderText("Weet je zeker dat je wil passen?");

        ButtonType buttonTypeCancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeOk = new ButtonType("OK");

        alert.getButtonTypes().setAll(buttonTypeCancel, buttonTypeOk);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonTypeOk) {
            this.passGame();
        }
    }

    private void passGame() {
        Connection connection = Singleton.getInstance().getConnection();
        String currentUsername = Singleton.getInstance().getUser().getUsername();
        boolean isPlayer1 = this.game.getUsernamePlayer1().equals(currentUsername);
        String typePlayer1 = "notSet";
        String typePlayer2 = "notSet";

        //Getting turn information of players
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT (SELECT turnaction_type FROM turnplayer1 WHERE game_id = ? AND turn_id = (SELECT MAX(turn_id) AS turn FROM `turn` WHERE game_id = ?) ORDER BY turn_id DESC) AS type_player1, (SELECT turnaction_type FROM turnplayer2 WHERE game_id = ? AND turn_id = (SELECT MAX(turn_id) AS turn FROM `turn` WHERE game_id = ?) ORDER BY turn_id DESC) AS type_player2");
            statement.setInt(1, this.game.getGameId());
            statement.setInt(2, this.game.getGameId());
            statement.setInt(3, this.game.getGameId());
            statement.setInt(4, this.game.getGameId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                typePlayer1 = resultSet.getString("type_player1");
                typePlayer2 = resultSet.getString("type_player2");
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        if (isPlayer1 && typePlayer1 == null) {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO turnplayer1(game_id, turn_id, username_player1, bonus, score, turnaction_type) VALUES (?, (SELECT (IFnull(MAX(turn_id), 0) + 1) AS next_turn FROM turnplayer1 t2 WHERE game_id = ?), ?, 0, 0, 'pass')");
                statement.setInt(1, this.game.getGameId());
                statement.setInt(2, this.game.getGameId());
                statement.setString(3, currentUsername);

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DbLoadException(e);
            } finally {
                if (typePlayer2 != null && typePlayer2.equals("pass")) {
                    this.giveNewLetterInHand();
                }
            }
        } else if (!isPlayer1 && typePlayer2 == null) {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO turnplayer2(game_id, turn_id, username_player1, bonus, score, turnaction_type) VALUES (?, (SELECT (IFnull(MAX(turn_id), 0) + 1) AS next_turn FROM turnplayer2 t2 WHERE game_id = ?), ?, 0, 0, 'pass')");
                statement.setInt(1, this.game.getGameId());
                statement.setInt(2, this.game.getGameId());
                statement.setString(3, currentUsername);

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DbLoadException(e);
            } finally {
                if (typePlayer1 != null && typePlayer1.equals("pass")) {
                    this.giveNewLetterInHand();
                }
            }
        } else {
            System.out.println("Je hebt deze beurt al iets gedaan");
        }
    }

    private void giveNewLetterInHand() {
        // TODO Check if game end
        // if max letters over <= 7 {
        //  this.endGame()
        // } else {

            // TODO Give new letters
            // Same as give new letters but first clear hand.

            // TODO Start new turn
            // Start new turn functions same as play a turn.
        // }
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

        HashMap<Character, Integer> symbolValues = this.getSymbolValues();

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

                wordPoints += symbolValues.get(tile.getValue()) * letterMultiplier;
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
