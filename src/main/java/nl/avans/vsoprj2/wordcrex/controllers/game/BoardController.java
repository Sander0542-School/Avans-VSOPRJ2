package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BoardTile;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.LetterTile;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class BoardController extends Controller {
    private Game game;
    private final Board board = new Board();

    private LetterTile selectedLetter;
    private boolean moveTileFromToBoard = false;
    private BoardTile previousBoardTile;
    private ArrayList<Letter> currentLetters = new ArrayList<>();

    @FXML
    private GridPane gameGrid;
    @FXML
    private HBox lettertiles;

    @FXML
    private Label player1Name;
    @FXML
    private Label player2Name;
    @FXML
    private Label player1Score;
    @FXML
    private Label player2Score;

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

        this.loadPlayerData();

        this.loadHandLetters();
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
                BoardTile boardTile = new BoardTile(this.board.getTile(x, y));
                this.setBoardTileClick(boardTile);
                this.gameGrid.add(boardTile, x - 1, y - 1);
            }
        }
    }

    public void loadPlayerData() {
        this.player1Name.setText(this.game.getUsernamePlayer1());
        this.player2Name.setText(this.game.getUsernamePlayer2());

        this.player1Score.setText(String.valueOf(this.game.getPlayerScore(true)));
        this.player2Score.setText(String.valueOf(this.game.getPlayerScore(false)));
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
        if (words == null) {
            return false;
        }

        List<String> wordStrings = this.getWordsFromList(words);

        for (String wordString : wordStrings) {
            if (!this.isExistingWord(wordString)) {
                return false;
            }
        }

        for (Tile tile : this.getUnconfirmedTiles()) {
            if (tile.getTileType() == Tile.TileType.START) {
                return true;
            }

            Board.Coordinate coordinate = this.board.getCoordinate(tile);
            if (this.board.hasConfirmedSurroundingTile(coordinate.getX(), coordinate.getY())) {
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

                wordPoints += tile.getWorth() * letterMultiplier;
            }

            wordPoints *= wordMultiplier;

            points.addPoints(wordPoints);
        }

        if (this.getUnconfirmedTiles().size() == 7 && words.size() != 0) points.addPoints(100);

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

    //hand out letters (previous turn winner)
    public void handOutLetters() {
        Connection connection = Singleton.getInstance().getConnection();
        int currentTurn = this.game.getCurrentTurn();
        int extraLetters = 7;
        this.currentLetters.clear();

        if (currentTurn > 0) {
            try {
                //get leftover letters from the previous turn
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT l.letter_id, l.game_id, l.symbol_letterset_code, l.symbol, s.value FROM `handletter` hl " +
                                "LEFT JOIN turnboardletter tbl ON hl.game_id = tbl.game_id AND hl.turn_id = tbl.turn_id AND hl.letter_id = tbl.letter_id " +
                                "INNER JOIN letter l ON hl.game_id = l.game_id AND hl.letter_id = l.letter_id " +
                                "INNER JOIN symbol s ON l.symbol_letterset_code = s.letterset_code AND l.symbol = s.symbol " +
                                "WHERE hl.game_id = ? AND hl.turn_id = ? AND tbl.letter_id IS NULL LIMIT 7"
                );
                statement.setInt(1, this.game.getGameId());
                statement.setInt(2, (currentTurn - 1));
                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    this.currentLetters.add(new Letter(result));
                    extraLetters--;
                }

            } catch (SQLException e) {
                throw new DbLoadException(e);
            }
        }

        this.currentLetters.addAll(this.getRandomLettersFromPool(extraLetters));

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO `handletter` (`game_id`,`turn_id`,`letter_id`) VALUES ");

            int handletterCount = 0;
            for (Letter letter : this.currentLetters) {
                if (handletterCount > 0) {
                    sb.append(",");
                }
                sb.append("(").append(this.game.getGameId()).append(", ").append(currentTurn).append(",").append(letter.getLetterId()).append(")");
                handletterCount++;
            }
            sb.append(";");

            PreparedStatement statement = connection.prepareStatement(sb.toString());
            int result = statement.executeUpdate();

            if (result > 0) {
                Collections.shuffle(this.currentLetters);
                this.displayLetters();
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public ArrayList<Letter> getRandomLettersFromPool(int extraLetters) {
        Connection connection = Singleton.getInstance().getConnection();
        ArrayList<Letter> letters = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT l.letter_id, l.game_id, l.symbol_letterset_code, l.symbol, s.value FROM `pot` p INNER JOIN letter l ON p.letter_id = l.letter_id AND p.game_id = l.game_id INNER JOIN symbol s ON l.symbol_letterset_code = s.letterset_code AND l.symbol = s.symbol WHERE p.game_id = ? ORDER BY RAND() LIMIT ?");
            statement.setInt(1, this.game.getGameId());
            statement.setInt(2, extraLetters);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                letters.add(new Letter(result));
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        return letters;
    }

    //get handed out letters (handed out by previous turn winner)
    public void loadHandLetters() {
        Connection connection = Singleton.getInstance().getConnection();
        int currentTurn = this.game.getCurrentTurn();
        this.currentLetters.clear();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT l.letter_id, l.game_id, l.symbol_letterset_code, l.symbol, s.value FROM `handletter` hl INNER JOIN letter l ON hl.letter_id = l.letter_id AND hl.game_id = l.game_id INNER JOIN symbol s ON l.symbol_letterset_code = s.letterset_code AND l.symbol = s.symbol WHERE hl.game_id = ? AND hl.turn_id = ? LIMIT 7");
            statement.setInt(1, this.game.getGameId());
            //TODO: Testing purpose: Remove "-7"
//            statement.setInt(2, currentTurn - 7);
            statement.setInt(2, currentTurn);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                this.currentLetters.add(new Letter(result));
            }

            this.displayLetters();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void displayLetters() {
        this.lettertiles.getChildren().removeIf(node -> node instanceof LetterTile);
        Collections.shuffle(this.currentLetters);

        for (Letter letter : this.currentLetters) {
            LetterTile letterTile = new LetterTile(letter);
            this.setLetterTileClick(letterTile);
            this.lettertiles.getChildren().add(letterTile);
        }
    }

    @FXML
    private void handleShuffleAction() {
        /*if(!this.getUnconfirmedTiles().isEmpty()){
            this.displayLetters(this.lettertiles);
        }*/
    }

    private void setLetterTileClick(LetterTile lettertile) {
        lettertile.setOnMouseClicked(event -> {
            if (this.selectedLetter == lettertile) {
                lettertile.deselectLetter();
                this.selectedLetter = null;
            } else {
                if (this.moveTileFromToBoard) {
                    this.previousBoardTile.setSelected(false);
                }
                this.selectLetter(lettertile);
            }
            this.moveTileFromToBoard = false;
        });
    }

    private void selectLetter(LetterTile lettertile) {
        if (this.selectedLetter != null) {
            this.selectedLetter.deselectLetter();
        }

        this.selectedLetter = lettertile;
        lettertile.selectLetter();
    }

    private void setBoardTileClick(BoardTile boardTile) {
        boardTile.setOnMouseClicked(event -> {
            if (this.selectedLetter != null) {
                if (this.selectedLetter == boardTile.getLetterTile()) {
                    boardTile.setSelected(false);
                    this.selectedLetter = null;
                } else {
                    if (!boardTile.getTile().hasLetter() && boardTile.getLetterTile() == null) {
                        boardTile.setLetterTile(this.selectedLetter);

                        if (this.moveTileFromToBoard) {
                            this.previousBoardTile.setLetterTile(null);
                            this.previousBoardTile.setSelected(false);
                            this.moveTileFromToBoard = false;
                        } else {
                            this.lettertiles.getChildren().remove(this.selectedLetter);
                        }

                        this.selectedLetter.deselectLetter();
                        this.selectedLetter = null;
                    } else {
                        if (!boardTile.getTile().isConfirmed()) {
                            if (this.previousBoardTile != null) {
                                this.previousBoardTile.setSelected(false);
                            }
                            this.previousBoardTile = boardTile;
                            this.moveTileFromToBoard = true;
                            this.selectLetter(boardTile.getLetterTile());
                            boardTile.setSelected(true);
                        }
                    }
                }
            }
            //move selected tile to another tile on board
            else {
                if (!boardTile.getTile().isConfirmed() && boardTile.getLetterTile() != null) {
                    this.previousBoardTile = boardTile;
                    this.moveTileFromToBoard = true;
                    this.selectLetter(boardTile.getLetterTile());
                    boardTile.setSelected(true);
                }
            }
        });
    }

    public void handleLettertilesClick() {
        if (this.selectedLetter != null && this.moveTileFromToBoard) {
            this.previousBoardTile.setLetterTile(null);
            this.previousBoardTile.updateBackgroundColor();
            this.moveTileFromToBoard = false;

            this.lettertiles.getChildren().add(this.selectedLetter);

            this.selectedLetter.deselectLetter();
            this.selectedLetter = null;
        }
    }

    private void gridSizeChanged() {
        double size = Math.min(this.gameGrid.getWidth(), this.gameGrid.getHeight());

        for (Node node : this.gameGrid.getChildren()) {
            ((BoardTile) node).setSize(size / (Board.BOARD_SIZE + 1));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.gameGrid.widthProperty().addListener((observable, oldValue, newValue) -> this.gridSizeChanged());
        this.gameGrid.heightProperty().addListener((observable, oldValue, newValue) -> this.gridSizeChanged());
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

    /**
     * Play button event, asks player if they want to play a turn
     */
    @FXML
    private void confirmLettersButtonClicked() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Bevestig Woord");
        confirmationDialog.setHeaderText("Weet je zeker dat je dit woord wil spelen?");
        Optional<ButtonType> dialogResult = confirmationDialog.showAndWait();

        if (dialogResult.isPresent()) {
            if (dialogResult.get() == ButtonType.OK) {
                List<List<Tile>> words = this.getWords();
                if (this.checkWords(words)) {
                    this.createNewPlayerTurn();
                } else {
                    //Throws alert if word is not correct
                    Alert invalidWordDialog = new Alert(Alert.AlertType.WARNING, "Dit is geen geldig woord.\nProbeer een ander woord.");
                    invalidWordDialog.setTitle("Fout Woord");
                    invalidWordDialog.showAndWait();
                }
            }
        }
    }

    /**
     * Creates a new turnPlayer record in the database
     * If both player have player their turn, a new turn is created
     */
    private void createNewPlayerTurn() {
        Connection connection = Singleton.getInstance().getConnection();
        boolean isPlayer1 = this.game.getUsernamePlayer1().equals(Singleton.getInstance().getUser().getUsername());

        try {
            //Insert into correct TurnPlayer table
            String turnPlayerQuery;
            if (isPlayer1) {
                turnPlayerQuery = "INSERT INTO `turnplayer1`(`game_id`, `turn_id`, `username_player1`, `bonus`, `score`, `turnaction_type`) VALUES (?,?,?,?,?,?)";
            } else {
                turnPlayerQuery = "INSERT INTO `turnplayer2`(`game_id`, `turn_id`, `username_player2`, `bonus`, `score`, `turnaction_type`) VALUES (?,?,?,?,?,?)";
            }

            //Insert into TurnPlayer table
            Points points = this.calculatePoints(this.getWords());
            PreparedStatement turnPlayerStatement = connection.prepareStatement(turnPlayerQuery);
            turnPlayerStatement.setInt(1, this.game.getGameId());
            turnPlayerStatement.setInt(2, this.game.getCurrentTurn());
            turnPlayerStatement.setString(3, isPlayer1 ? this.game.getUsernamePlayer1() : this.game.getUsernamePlayer2());
            turnPlayerStatement.setInt(4, points.getBonus());
            turnPlayerStatement.setInt(5, points.getPoints());
            turnPlayerStatement.setString(6, ScoreboardRound.TurnActionType.PLAY.toString().toLowerCase());
            turnPlayerStatement.executeUpdate();

            //Get other players turn
            String otherPlayerTurnQuery;
            if (isPlayer1) {
                otherPlayerTurnQuery = "SELECT * FROM `turnplayer2` WHERE `game_id` = ? AND `turn_id` = ? AND `username_player2` = ?";
            } else {
                otherPlayerTurnQuery = "SELECT * FROM `turnplayer1` WHERE `game_id` = ? AND `turn_id` = ? AND `username_player1` = ?";
            }

            //Get other players turn
            PreparedStatement otherPlayerTurnStatement = connection.prepareStatement(otherPlayerTurnQuery);
            otherPlayerTurnStatement.setInt(1, this.game.getGameId());
            otherPlayerTurnStatement.setInt(2, this.game.getCurrentTurn());
            otherPlayerTurnStatement.setString(3, isPlayer1 ? this.game.getUsernamePlayer2() : this.game.getUsernamePlayer1());
            ResultSet otherPlayerTurnResultSet = otherPlayerTurnStatement.executeQuery();

            if (otherPlayerTurnResultSet.next()) {
                //If players have the same score.. the first player gets the bonus
                if (otherPlayerTurnResultSet.getInt("score") == points.getPoints()) {
                    String updateBonusQuery = (isPlayer1 ? "UPDATE `turnplayer2` SET `bonus` = ?" : "UPDATE `turnplayer1` SET `bonus` = ?") +
                            " WHERE `game_id` = ? AND `turn_id` = ?";
                    PreparedStatement updateBonusStatement = connection.prepareStatement(updateBonusQuery);
                    updateBonusStatement.setInt(1, 5);
                    updateBonusStatement.setInt(2, this.game.getGameId());
                    updateBonusStatement.setInt(3, this.game.getCurrentTurn());
                    updateBonusStatement.executeUpdate();
                }
                this.createNewTurn();
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    /**
     * Creates a new turn in the database
     */
    private void createNewTurn() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            int newTurnId = this.game.getCurrentTurn() + 1;

            PreparedStatement newTurnStatement = connection.prepareStatement("INSERT INTO `turn`(`game_id`, `turn_id`) VALUES (?,?)");
            newTurnStatement.setInt(1, this.game.getGameId());
            newTurnStatement.setInt(2, newTurnId);
            newTurnStatement.executeUpdate();

            this.handOutLetters();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
