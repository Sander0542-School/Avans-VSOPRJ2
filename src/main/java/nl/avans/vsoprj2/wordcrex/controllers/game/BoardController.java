package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BackgroundTile;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.LetterTile;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.*;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import static nl.avans.vsoprj2.wordcrex.models.Board.TileType.START;

public class BoardController extends Controller {
    private Game game;
    private Board board;

    private List<Tile> unconfirmedTiles = new ArrayList<>();
    private HashMap<Character, Integer> symbolValues = new HashMap<>();

    public BoardController() {
        this.getSymbolValues();
    }

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

    private void getSymbolValues() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `symbol`, `value` FROM `symbol` WHERE `letterset_code` = ?");
            statement.setString(1, this.game.getLettersetCode());

            ResultSet symbolSet = statement.executeQuery();

            while (symbolSet.next()) {
                this.symbolValues.put(symbolSet.getString(1).charAt(0), symbolSet.getInt(2));
            }

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

    /**
     *
     */
    @FXML
    private void confirmLettersButtonClicked() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Bevestig Woord");
        confirmationDialog.setHeaderText("Weet je zeker dat je dit woord wil spelen?");
        Optional<ButtonType> dialogResult = confirmationDialog.showAndWait();

        if (dialogResult.isPresent())
            if (dialogResult.get() == ButtonType.OK) {
                List<List<Tile>> words = this.getWords();
                if (this.checkWords(words)) {
                    this.createNewPlayerTurn();
                } else {
                    //Throws alert if word is not correct
                    Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Dit is geen geldig woord.\nProbeer een ander woord.");
                    invalidWordDialog.setTitle("Fout Woord");
                    invalidWordDialog.showAndWait();
                }
            }
    }

    /**
     * Creates a new turnPlayer record
     * If both player have player their turn, a new turn is created
     */
    private void createNewPlayerTurn() {
        Connection connection = Singleton.getInstance().getConnection();
        boolean isPlayer1 = this.game.getUsernamePlayer1().equals(Singleton.getInstance().getUser().getUsername());
        int currentTurnId = 0;

        try {
            //Get the current turn
            PreparedStatement turnStatement = connection.prepareStatement("SELECT MAX(`turn_id`) FROM `turn` WHERE `game_id` = ?");
            turnStatement.setInt(1, this.game.getGameId());
            ResultSet turn = turnStatement.executeQuery();
            while (turn.next()) {
                currentTurnId = turn.getInt(1);
            }

            //Game has no turn prints error
            if (currentTurnId == 0) {
                System.err.println("Kan geen beurten van het huidige spel ophalen...");
                return;
            }

            //Insert into correct TurnPlayer table
            String turnPlayerQuery;
            if (isPlayer1) {
                turnPlayerQuery = "INSERT INTO `turnplayer1`(`game_id`, `turn_id`, `username_player1`, `bonus`, `score`, `turnaction_type`) VALUES (?,?,?,?,?,?)";
            } else {
                turnPlayerQuery = "INSERT INTO `turnplayer2`(`game_id`, `turn_id`, `username_player2`, `bonus`, `score`, `turnaction_type`) VALUES (?,?,?,?,?,?)";
            }
            PreparedStatement turnPlayerStatement = connection.prepareStatement(turnPlayerQuery);
            turnPlayerStatement.setInt(1, this.game.getGameId());
            turnPlayerStatement.setInt(2, currentTurnId);
            turnPlayerStatement.setString(3, isPlayer1 ? this.game.getUsernamePlayer1() : this.game.getUsernamePlayer2());
            turnPlayerStatement.setInt(4, 0); //Bonus ???
            turnPlayerStatement.setInt(5, this.calculatePoints(this.getWords()));
            turnPlayerStatement.setString(6, ScoreboardRound.TurnActionType.PLAY.toString().toLowerCase());
            turnPlayerStatement.executeQuery();

            //Get other players turn
            String otherPlayerTurnQuery;
            if (isPlayer1) {
                otherPlayerTurnQuery = "SELECT * FROM `turnplayer2` WHERE `game_id` = ? AND `turn_id` = ? AND `username_player2` = ?";
            } else {
                otherPlayerTurnQuery = "SELECT * FROM `turnplayer1` WHERE `game_id` = ? AND `turn_id` = ? AND `username_player1` = ?";
            }

            PreparedStatement otherPlayerTurnStatement = connection.prepareStatement(otherPlayerTurnQuery);
            otherPlayerTurnStatement.setInt(1, this.game.getGameId());
            otherPlayerTurnStatement.setInt(2, currentTurnId);
            otherPlayerTurnStatement.setString(3, isPlayer1 ? this.game.getUsernamePlayer2() : this.game.getUsernamePlayer1());
            ResultSet otherPlayerTurnResultSet = otherPlayerTurnStatement.executeQuery();

            //if other player has played their turn, create a new turn
            if(otherPlayerTurnResultSet.next()) {
                this.createNewTurn();
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void createNewTurn() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            int newTurnId = 0;
            //Creates new turn_id
            PreparedStatement getPreviousTurnStatement = connection.prepareStatement("SELECT MAX(`turn_id`) FROM `turn` WHERE `game_id` = ?");
            getPreviousTurnStatement.setInt(1, this.game.getGameId());
            ResultSet turn = getPreviousTurnStatement.executeQuery();
            while (turn.next()) {
                newTurnId = turn.getInt(1) + 1;
            }

            PreparedStatement newTurnStatement = connection.prepareStatement("INSERT INTO `turn`(`game_id`, `turn_id`) VALUES (?,?)");
            newTurnStatement.setInt(1, this.game.getGameId());
            newTurnStatement.setInt(2, newTurnId);
            ResultSet newTurnResultSet = newTurnStatement.executeQuery();

            //create new hand
            //TODO to be created
        } catch(SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
