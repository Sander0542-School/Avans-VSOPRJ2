package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.BoardTile;
import nl.avans.vsoprj2.wordcrex.controls.gameboard.LetterTile;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.*;
import nl.avans.vsoprj2.wordcrex.utils.NumberUtil;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class BoardController extends Controller {
    private static final double DISABLED_OPACITY = 0.5;
    private Game game;
    private final Board board = new Board();
    private boolean turnLocked;
    private final Timer timer = new Timer();

    private LetterTile selectedLetter;
    private boolean moveTileFromToBoard = false;
    private BoardTile previousBoardTile;
    private final ArrayList<Letter> currentLetters = new ArrayList<>();
    private final ContextMenu gameOptionsMenu = new ContextMenu();
    private HashMap<Character, Integer> symbolValues;

    private int turnId;
    private int playerOneScore;
    private int playerTwoScore;

    @FXML
    private Label potSizeLabel;
    @FXML
    private ImageView passTurnButton;
    @FXML
    private ImageView playTurnButton;
    @FXML
    private GridPane gameGrid;
    @FXML
    private HBox lettertiles;
    @FXML
    private Label boardScore;
    @FXML
    private Label player1Name;
    @FXML
    private Label player2Name;
    @FXML
    private Label player1Score;
    @FXML
    private Label player2Score;
    @FXML
    private ImageView shuffleReturnImage;

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
        this.symbolValues = this.getSymbolValues();

        this.loadAndRenderGame();

        this.timer.scheduleAtFixedRate(this.createTimerTask(), 0, 10000);

        if (game.getCurrentTurn() == 0) this.createNewTurn(false);
    }

    private void loadAndRenderGame() {
        this.turnLocked = this.game.getTurnLocked();
        this.turnId = this.game.getCurrentTurn();

        this.board.loadLetters(this.game, this.symbolValues);
        this.renderBoard();

        this.fetchPlayerData();
        this.renderPlayerData();

        this.loadHandLetters();
        this.displayLetters();
        this.updateShuffleReturnButton();
    }

    /**
     * TimerTask refreshes the board if the score or turn id has changed. Only when turn is locked in the client!!!
     *
     * @return TimerTask to refresh board
     */
    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                final Game.GameState gameState = BoardController.this.game.getCurrentState();
                if (BoardController.this.game.getOwnGame() && BoardController.this.game.getGameState() != gameState && (gameState == Game.GameState.FINISHED || gameState == Game.GameState.RESIGNED)) {
                    BoardController.this.timer.cancel();
                    BoardController.this.timer.purge();

                    String message;
                    switch (gameState) {
                        case FINISHED:
                            message = "Een speler heeft het spel gewonnen.";
                            break;
                        case RESIGNED:
                            message = "Een speler heeft opgegeven.";
                            break;
                        default:
                            message = "";
                    }
                    String finalMessage = message;

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, finalMessage);
                        alert.setHeaderText(null);
                        alert.setTitle("Het spel is afgelopen");
                        alert.showAndWait();

                        BoardController.this.navigateTo(BoardController.this.game.getOwnGame() ? "/views/games.fxml" : "/views/observer.fxml");
                    });
                }

                if (BoardController.this.turnLocked) {
                    if (WordCrex.DEBUG_MODE) System.out.println("BoardController: Timer task loading data");

                    int originalPlayerOneScore = BoardController.this.playerOneScore;
                    int originalPlayerTwoScore = BoardController.this.playerTwoScore;
                    int newTurnId = BoardController.this.game.getCurrentTurn();
                    BoardController.this.board.loadLetters(BoardController.this.game, BoardController.this.symbolValues);
                    BoardController.this.loadHandLetters();
                    BoardController.this.fetchPlayerData();
                    BoardController.this.turnLocked = BoardController.this.game.getTurnLocked();

                    if (BoardController.this.playerOneScore != originalPlayerOneScore ||
                            BoardController.this.playerTwoScore != originalPlayerTwoScore ||
                            BoardController.this.turnId != newTurnId) {
                        Platform.runLater(() -> {
                            if (WordCrex.DEBUG_MODE) System.out.println("BoardController: Timer task rendering data");
                            BoardController.this.turnId = newTurnId;
                            BoardController.this.renderBoard();
                            BoardController.this.renderPlayerData();
                            BoardController.this.displayLetters();
                        });
                    }
                }
            }
        };
    }

    private List<BoardTile> getUnconfirmedTiles() {
        List<BoardTile> tiles = new ArrayList<>();

        for (Node node : this.gameGrid.getChildren()) {
            BoardTile boardTile = (BoardTile) node;

            if (boardTile.getLetterTile() != null) {
                tiles.add(boardTile);
            }
        }
        return tiles;
    }

    /**
     * Renders all the tiles to the board and adds click event handler to them
     */
    private void renderBoard() {
        this.gameGrid.getChildren().clear();

        if (this.turnLocked) {
            this.passTurnButton.setOpacity(BoardController.DISABLED_OPACITY);
            this.playTurnButton.setOpacity(BoardController.DISABLED_OPACITY);
            this.lettertiles.setOpacity(BoardController.DISABLED_OPACITY);
        } else {
            this.passTurnButton.setOpacity(1.0);
            this.playTurnButton.setOpacity(1.0);
            this.lettertiles.setOpacity(1.0);
        }

        for (int x = 1; x <= Board.BOARD_SIZE; x++) {
            for (int y = 1; y <= Board.BOARD_SIZE; y++) {
                BoardTile boardTile = new BoardTile(this.board.getTile(x, y));
                this.setBoardTileClick(boardTile);
                this.gameGrid.add(boardTile, x - 1, y - 1);
            }
        }

        this.gridSizeChanged();
    }

    /**
     * Fetches the player score and saves it in a variable
     */
    private void fetchPlayerData() {
        this.playerOneScore = this.game.getPlayerScore(true);
        this.playerTwoScore = this.game.getPlayerScore(false);
    }

    /**
     * Renders the player score from the variables to the GUI
     */
    private void renderPlayerData() {
        this.player1Name.setText(this.game.getUsernamePlayer1());
        this.player2Name.setText(this.game.getUsernamePlayer2());

        this.player1Score.setText(String.valueOf(this.playerOneScore));
        this.player2Score.setText(String.valueOf(this.playerTwoScore));
    }

    private void renderAndFetchRemainingTiles() {
        int potSize = this.game.getAmountOfPoolLetters();
        this.potSizeLabel.setText(potSize + " tegels resterend");
    }

    private void endGame() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT (SELECT (SUM(bonus) + SUM(score)) FROM turnplayer1 WHERE game_id = ?) AS Player1, (SELECT (SUM(bonus) + SUM(score)) FROM turnplayer2 WHERE game_id = ?) AS Player2");
            statement.setInt(1, this.game.getGameId());
            statement.setInt(2, this.game.getGameId());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getInt("Player1") > resultSet.getInt("Player2")) {
                    this.game.setGameState(Game.GameState.FINISHED);
                    this.game.setWinner(this.game.getUsernamePlayer1());
                    this.game.save();
                } else {
                    this.game.setGameState(Game.GameState.FINISHED);
                    this.game.setWinner(this.game.getUsernamePlayer2());
                    this.game.save();
                }
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Kon het spel niet be??indigen.\nProbeer het opnieuw.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void handlePassGame() {
        if (this.turnLocked) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game passen");
        alert.setHeaderText("Weet je zeker dat je wil passen?");

        ButtonType buttonTypeCancel = new ButtonType("Nee", ButtonBar.ButtonData.NO);
        ButtonType buttonTypeOk = new ButtonType("Ja", ButtonBar.ButtonData.YES);

        alert.getButtonTypes().setAll(buttonTypeCancel, buttonTypeOk);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                this.passGame();
            }
        });
    }

    @FXML
    private void handleBackButton() {
        this.timer.cancel();
        this.timer.purge();
        this.navigateTo(this.game.getOwnGame() ? "/views/games.fxml" : "/views/observer.fxml");
    }

    @FXML
    private void handleScoreboardAction() {
        this.timer.cancel();
        this.timer.purge();
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
        this.timer.cancel();
        this.timer.purge();
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

    @FXML
    private void handleGameOptionsMenu(MouseEvent event) {
        this.gameOptionsMenu.show(this.getStage(), event.getScreenX(), event.getScreenY());
    }

    @FXML
    private void gameOptionsMenuEventHandler(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();

        switch (menuItem.getId()) {
            case "geef op":
                if (!this.game.getOwnGame()) return;
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Het spel opgeven");
                alert.setHeaderText(null);
                alert.setContentText("Weet je zeker dat je wilt opgeven?");
                ButtonType okButton = new ButtonType("Ja", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Nee", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(okButton, noButton);
                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                        this.game.resignGame();
                        this.navigateTo("/views/games.fxml");
                    }
                });
                break;
        }
    }

    @FXML
    private void handleShuffleReturnAction() {
        if (this.getUnconfirmedTiles().isEmpty()) {
            //shuffle
            this.displayLetters();
            this.selectedLetter = null;
        } else {
            //return letters
            for (BoardTile boardTile : this.getUnconfirmedTiles()) {
                boardTile.setLetterTile(null);
                boardTile.updateBackgroundColor();
                this.moveTileFromToBoard = false;

                if (this.selectedLetter != null) {
                    this.selectedLetter.deselectLetter();
                    this.selectedLetter = null;
                }
            }

            this.displayLetters();
            this.updateShuffleReturnButton();
            this.updatePoints();
        }
    }

    @FXML
    private void handleLettertilesClick() {
        if (this.turnLocked) return;

        if (this.selectedLetter != null && this.moveTileFromToBoard) {
            this.previousBoardTile.setLetterTile(null);
            this.previousBoardTile.updateBackgroundColor();
            this.moveTileFromToBoard = false;

            this.lettertiles.getChildren().add(this.selectedLetter);

            this.selectedLetter.deselectLetter();
            this.selectedLetter = null;
        }

        this.updateShuffleReturnButton();
        this.updatePoints();
    }

    /**
     * Play button event, asks player if they want to play a turn
     */
    @FXML
    private void confirmLettersButtonClicked() {
        if (this.turnLocked) return;
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Bevestig Woord");
        confirmationDialog.setHeaderText("Weet je zeker dat je dit woord wil spelen?");
        Optional<ButtonType> dialogResult = confirmationDialog.showAndWait();

        if (dialogResult.isPresent()) {
            if (dialogResult.get() == ButtonType.OK) {
                List<List<BoardTile>> words = this.getWords();
                if (this.checkWords(words)) {
                    int turn = this.game.getCurrentTurn();
                    this.createNewPlayerTurn(turn);
                    this.createNewPlayerBoard(turn, this.getUnconfirmedTiles());
                } else {
                    //Throws alert if word is not correct
                    Alert invalidWordDialog = new Alert(Alert.AlertType.WARNING, "Dit is geen geldig woord.\nProbeer een ander woord.");
                    invalidWordDialog.setHeaderText(null);
                    invalidWordDialog.setTitle("Fout Woord");
                    invalidWordDialog.showAndWait();
                }
            }
        }
    }

    private void passGame() {
        Connection connection = Singleton.getInstance().getConnection();

        int turn = this.game.getCurrentTurn();

        String currentUsername = Singleton.getInstance().getUser().getUsername();
        boolean isPlayer1 = this.game.getUsernamePlayer1().equals(currentUsername);

        StringBuilder turnPlayerQueryBuilder = new StringBuilder();
        turnPlayerQueryBuilder.append("INSERT INTO `");
        turnPlayerQueryBuilder.append(isPlayer1 ? "turnplayer1" : "turnplayer2");
        turnPlayerQueryBuilder.append("` (`game_id`, `turn_id`, `");
        turnPlayerQueryBuilder.append(isPlayer1 ? "username_player1" : "username_player2");
        turnPlayerQueryBuilder.append("`, `bonus`, `score`, `turnaction_type`) VALUES (?, ?, ?, 0, 0, 'pass');");

        try {
            PreparedStatement turnPlayerStatement = connection.prepareStatement(turnPlayerQueryBuilder.toString());
            turnPlayerStatement.setInt(1, this.game.getGameId());
            turnPlayerStatement.setInt(2, turn);
            turnPlayerStatement.setString(3, currentUsername);

            turnPlayerStatement.executeUpdate();

            StringBuilder turnPlayerQueryBuilder2 = new StringBuilder();

            turnPlayerQueryBuilder2.append("SELECT `cp`.`game_id`, `cp`.`turn_id`, `cp`.`turnaction_type` as cp_type, `op`.`turnaction_type` as op_type FROM `");
            turnPlayerQueryBuilder2.append(isPlayer1 ? "turnplayer1" : "turnplayer2");
            turnPlayerQueryBuilder2.append("` cp INNER JOIN `");
            turnPlayerQueryBuilder2.append(isPlayer1 ? "turnplayer2" : "turnplayer1");
            turnPlayerQueryBuilder2.append("` op ON `cp`.`game_id` = `op`.`game_id` AND `cp`.`turn_id` = `op`.`turn_id` WHERE `cp`.`game_id` = ? AND `cp`.`turn_id` = ?;");

            PreparedStatement turnPlayerStatement2 = connection.prepareStatement(turnPlayerQueryBuilder2.toString());
            turnPlayerStatement2.setInt(1, this.game.getGameId());
            turnPlayerStatement2.setInt(2, turn);

            ResultSet turnPlayerResultSet2 = turnPlayerStatement2.executeQuery();

            if (turnPlayerResultSet2.next()) {
                if (turnPlayerResultSet2.getString("cp_type").equals("pass") && turnPlayerResultSet2.getString("op_type").equals("pass")) {
                    this.giveNewLetterInHand();
                } else {
                    this.setBoardPassWinner(!isPlayer1);
                }
            }
            this.loadAndRenderGame();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game pass");
            alert.setHeaderText("Je hebt deze beurt al iets gedaan. Je kunt niet opnieuw passen.");
            alert.showAndWait();
        }

        this.boardScore.setVisible(false);
    }

    private void setBoardPassWinner(boolean isPlayer1) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            StringBuilder turnBoardLetterQueryBuilder = new StringBuilder();
            turnBoardLetterQueryBuilder.append("INSERT INTO `turnboardletter` (`game_id`, `turn_id`, `letter_id`, `tile_x`, `tile_y`) SELECT `game_id`, `turn_id`, `letter_id`, `tile_x`, `tile_y` FROM `");
            turnBoardLetterQueryBuilder.append(isPlayer1 ? "boardplayer1" : "boardplayer2");
            turnBoardLetterQueryBuilder.append("` WHERE `game_id` = ? AND `turn_id` = ?;");

            PreparedStatement turnBoardLetterStatement = connection.prepareStatement(turnBoardLetterQueryBuilder.toString());
            turnBoardLetterStatement.setInt(1, this.game.getGameId());
            turnBoardLetterStatement.setInt(2, this.game.getCurrentTurn());

            turnBoardLetterStatement.executeUpdate();

            this.createNewTurn(false);
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    private void giveNewLetterInHand() {
        if (this.game.getAmountOfPoolLetters() < 7 || (this.game.passedThreeTimesInARow() && WordCrex.DEBUG_MODE)) {
            this.endGame();
        } else {
            this.createNewTurn(true);
        }
    }

    private boolean isExistingWord(String word) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'accepted');");
            statement.setString(1, word);
            statement.setString(2, this.game.getLettersetCode());
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    private void updatePoints() {
        List<List<BoardTile>> words = this.getWords();

        this.boardScore.setVisible(false);

        if (this.checkWords(words)) {
            Points points = this.calculatePoints(words);

            this.setBoardScorePosition();

            this.boardScore.setText(String.valueOf(points.points));

            this.boardScore.setVisible(true);
        }
    }

    private boolean checkWords(List<List<BoardTile>> words) {
        if (words == null) {
            return false;
        }

        List<String> wordStrings = this.getWordsFromList(words);

        for (String wordString : wordStrings) {
            if (!this.isExistingWord(wordString)) {
                return false;
            }
        }

        for (BoardTile boardTile : this.getUnconfirmedTiles()) {
            if (boardTile.getTile().getTileType() == Tile.TileType.START) {
                return true;
            }

            Board.Coordinate coordinate = this.board.getCoordinate(boardTile.getTile());
            if (this.board.hasConfirmedSurroundingTile(coordinate.getX(), coordinate.getY())) {
                return true;
            }
        }

        return false;
    }

    private List<List<BoardTile>> getWords() {
        Orientation orientation = this.getWordOrientation();
        List<BoardTile> unconfirmedTiles = this.getUnconfirmedTiles();

        List<List<BoardTile>> words = new ArrayList<>();

        if (orientation == null) {
            return null;
        }

        switch (orientation) {
            case SINGLE_TILE:
                words.add(this.findWord(unconfirmedTiles.get(0), true));
                words.add(this.findWord(unconfirmedTiles.get(0), false));
                break;
            case VERTICAL:
            case HORIZONTAL:
                boolean horizontal = orientation == Orientation.HORIZONTAL;
                words.add(this.findWord(unconfirmedTiles.get(0), horizontal));

                for (BoardTile boardTile : unconfirmedTiles) {
                    words.add(this.findWord(boardTile, !horizontal));
                }
                break;
        }

        words.removeIf(Objects::isNull);

        return words.size() > 0 ? words : null;
    }

    private Coordinates getCoordinates(List<BoardTile> tiles) {
        Coordinates coordinates = null;

        for (BoardTile boardTile : tiles) {
            Board.Coordinate coordinate = this.board.getCoordinate(boardTile.getTile());
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

    private Points calculatePoints(List<List<BoardTile>> words) {
        Points points = new Points();

        for (List<BoardTile> word : words) {
            int wordPoints = 0;
            int wordMultiplier = 1;

            for (BoardTile boardTile : word) {
                int letterMultiplier = 1;

                if (!boardTile.getTile().isConfirmed()) {
                    switch (boardTile.getTile().getTileType()) {
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

                wordPoints += boardTile.getWorth() * letterMultiplier;
            }

            wordPoints *= wordMultiplier;

            points.addPoints(wordPoints);
        }

        if (this.getUnconfirmedTiles().size() == 7 && words.size() != 0) points.addPoints(100);

        return points;
    }

    private List<String> getWordsFromList(List<List<BoardTile>> words) {
        List<String> wordsString = new ArrayList<>();

        for (List<BoardTile> word : words) {
            wordsString.add(word.stream().map(BoardTile::getLetter).collect(Collectors.joining()));
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
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    private List<BoardTile> findWord(BoardTile boardTile, boolean horizontal) {
        List<BoardTile> wordTiles = new ArrayList<>();
        BoardTile firstTile = boardTile;
        int i = 1;

        Board.Coordinate coordinate = this.board.getCoordinate(boardTile.getTile());
        int xCord = coordinate.getX();
        int yCord = coordinate.getY();

        if (horizontal) {
            while (this.hasTileAndLetter(xCord - i, yCord)) {
                firstTile = this.getBoardTile(xCord - i, yCord);
                i++;
            }
            wordTiles.add(firstTile);
            i -= 2;
            while (this.hasTileAndLetter(xCord - i, yCord)) {
                wordTiles.add(this.getBoardTile(xCord - i, yCord));
                i--;
            }
        } else {
            while (this.hasTileAndLetter(xCord, yCord - i)) {
                firstTile = this.getBoardTile(xCord, yCord - i);
                i++;
            }
            wordTiles.add(firstTile);
            i -= 2;
            while (this.hasTileAndLetter(xCord, yCord - i)) {
                wordTiles.add(this.getBoardTile(xCord, yCord - i));
                i--;
            }
        }

        return wordTiles.size() > 1 ? wordTiles : null;
    }

    private boolean hasTileAndLetter(int x, int y) {
        BoardTile boardTile = this.getBoardTile(x, y);

        if (boardTile != null) {
            return boardTile.getLetter() != null;
        }

        return false;
    }

    private BoardTile getBoardTile(int x, int y) {
        FilteredList<Node> nodes = this.gameGrid.getChildren().filtered(node -> GridPane.getColumnIndex(node) == (x - 1)).filtered(node -> GridPane.getRowIndex(node) == (y - 1));

        return nodes.isEmpty() ? null : (BoardTile) nodes.get(0);
    }

    private Orientation getWordOrientation() {
        List<BoardTile> unconfirmedTiles = this.getUnconfirmedTiles();

        if (unconfirmedTiles.size() == 0) {
            return null;
        } else if (unconfirmedTiles.size() == 1) {
            return Orientation.SINGLE_TILE;
        }

        Coordinates coordinates = this.getCoordinates(unconfirmedTiles);

        List<Integer> differentXValues = unconfirmedTiles.stream().map(boardTile -> this.board.getCoordinate(boardTile.getTile()).getX()).distinct().collect(Collectors.toList());
        List<Integer> differentYValues = unconfirmedTiles.stream().map(boardTile -> this.board.getCoordinate(boardTile.getTile()).getY()).distinct().collect(Collectors.toList());

        if (differentXValues.size() > 1 && differentYValues.size() > 1) {
            return null;
        }

        if (differentXValues.size() == 1) {
            for (int y = coordinates.minY; y <= coordinates.maxY; y++) {
                if (!this.hasTileAndLetter(differentXValues.get(0), y)) {
                    return null;
                }
            }

            return Orientation.VERTICAL;
        } else {
            for (int x = coordinates.minX; x <= coordinates.maxX; x++) {
                if (!this.hasTileAndLetter(x, differentYValues.get(0))) {
                    return null;
                }
            }

            return Orientation.HORIZONTAL;
        }
    }

    /**
     * hand out letters (previous turn winner)
     *
     * @param isPassedTurn this should be true if the turn is passed
     */
    private void handOutLetters(int currentTurn, boolean isPassedTurn) {
        if (WordCrex.DEBUG_MODE) System.out.println("BoardController: Handing out new letters");
        Connection connection = Singleton.getInstance().getConnection();
        int extraLetters = 7;
        this.currentLetters.clear();

        if (currentTurn > 0 && !isPassedTurn) {
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
                WordCrex.handleException(e);

                throw new DbLoadException(e);
            }
        }

        this.currentLetters.addAll(this.getRandomLettersFromPool(extraLetters, this.currentLetters));

        // End game finally implemented...
        if (this.currentLetters.isEmpty()) {
            this.endGame();
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO `handletter` (`game_id`,`turn_id`,`letter_id`) VALUES ");

            for (Letter letter : this.currentLetters) {
                sb.append(String.format("(%s, %s, %s),", this.game.getGameId(), currentTurn, letter.getLetterId()));
            }
            sb.setLength(sb.length() - 1);
            sb.append(";");

            PreparedStatement statement = connection.prepareStatement(sb.toString());
            int result = statement.executeUpdate();

            if (result > 0) {
                Collections.shuffle(this.currentLetters);
                this.displayLetters();
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    /**
     * Gets random letters from the pool
     *
     * @param extraLetters amount of letters to be returned
     *
     * @return ArrayList of Letters
     */
    private ArrayList<Letter> getRandomLettersFromPool(int extraLetters, List<Letter> currentLetters) {
        Connection connection = Singleton.getInstance().getConnection();
        ArrayList<Letter> letters = new ArrayList<>();

        try {
            StringBuilder queryBuilder = new StringBuilder();

            queryBuilder.append("SELECT l.letter_id, l.game_id, l.symbol_letterset_code, l.symbol, s.value FROM `pot` p INNER JOIN letter l ON p.letter_id = l.letter_id AND p.game_id = l.game_id INNER JOIN symbol s ON l.symbol_letterset_code = s.letterset_code AND l.symbol = s.symbol WHERE p.game_id = ? ");

            if (currentLetters.size() > 0) {
                queryBuilder.append("AND l.letter_id NOT IN (");
                queryBuilder.append(currentLetters.stream().map(letter -> String.valueOf(letter.getLetterId())).collect(Collectors.joining(",")));
                queryBuilder.append(") ");
            }

            queryBuilder.append("ORDER BY RAND() LIMIT ?");

            PreparedStatement statement = connection.prepareStatement(queryBuilder.toString());
            statement.setInt(1, this.game.getGameId());
            statement.setInt(2, extraLetters);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                letters.add(new Letter(result));
            }

        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }

        return letters;
    }

    /**
     * get handed out letters (handed out by previous turn winner)
     */
    private void loadHandLetters() {
        Connection connection = Singleton.getInstance().getConnection();
        int currentTurn = this.game.getCurrentTurn();
        this.currentLetters.clear();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT l.letter_id, l.game_id, l.symbol_letterset_code, l.symbol, s.value FROM `handletter` hl INNER JOIN letter l ON hl.letter_id = l.letter_id AND hl.game_id = l.game_id INNER JOIN symbol s ON l.symbol_letterset_code = s.letterset_code AND l.symbol = s.symbol WHERE hl.game_id = ? AND hl.turn_id = ? LIMIT 7");
            statement.setInt(1, this.game.getGameId());
            statement.setInt(2, currentTurn);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                this.currentLetters.add(new Letter(result));
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    private void displayLetters() {
        this.renderAndFetchRemainingTiles();
        this.lettertiles.getChildren().removeIf(node -> node instanceof LetterTile);
        Collections.shuffle(this.currentLetters);

        Connection connection = Singleton.getInstance().getConnection();

        List<Integer> playedTiles = new ArrayList<>();

        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT `letter_id` FROM `");
            query.append(Singleton.getInstance().getUser().getUsername().equals(this.game.getUsernamePlayer1()) ? "boardplayer1" : "boardplayer2");
            query.append("` WHERE `game_id` = ? AND `turn_id` = ?");

            PreparedStatement statement = connection.prepareStatement(query.toString());

            statement.setInt(1, this.game.getGameId());
            statement.setInt(2, this.game.getCurrentTurn());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                playedTiles.add(resultSet.getInt("letter_id"));
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);
        }

        for (Letter letter : this.currentLetters) {
            if (!playedTiles.contains(letter.getLetterId())) {
                LetterTile letterTile = new LetterTile(letter);
                this.setLetterTileClick(letterTile);
                this.lettertiles.getChildren().add(letterTile);
            }
        }
    }

    private void updateShuffleReturnButton() {
        if (this.getUnconfirmedTiles().isEmpty()) {
            this.shuffleReturnImage.setImage(new Image("/images/drawables/shuffle.png"));
        } else {
            this.shuffleReturnImage.setImage(new Image("/images/drawables/restore.png"));
        }
    }

    /**
     * Sets the click event handler on the entered letterTile
     *
     * @param lettertile the tile to add the click event handler to
     */
    private void setLetterTileClick(LetterTile lettertile) {
        lettertile.setOnMouseClicked(event -> {
            if (this.turnLocked) return;
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

            this.updatePoints();
        });
    }

    /**
     * Selects the letter
     *
     * @param lettertile letter to select
     */
    private void selectLetter(LetterTile lettertile) {
        if (this.selectedLetter != null) {
            this.selectedLetter.deselectLetter();
        }

        this.selectedLetter = lettertile;
        lettertile.selectLetter();
    }

    /**
     * Add click handler to boardTile
     *
     * @param boardTile tile to add the click handler to
     */
    private void setBoardTileClick(BoardTile boardTile) {
        boardTile.setOnMouseClicked(event -> {
            if (this.turnLocked) return;
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

            this.updateShuffleReturnButton();

            this.updatePoints();
        });
    }

    private void gridSizeChanged() {
        double size = Math.min(this.gameGrid.getWidth(), this.gameGrid.getHeight());

        for (Node node : this.gameGrid.getChildren()) {
            ((BoardTile) node).setSize(size / (Board.BOARD_SIZE + 1));
        }

        this.setBoardScorePosition();
    }

    private void setBoardScorePosition() {
        List<BoardTile> unconfirmedTiles = this.getUnconfirmedTiles();

        if (!unconfirmedTiles.isEmpty()) {
            Coordinates coordinates = this.getCoordinates(unconfirmedTiles);
            BoardTile boardTile = this.getBoardTile(coordinates.maxX, coordinates.maxY);

            double margin = boardTile.getHeight() - 6;
            this.boardScore.setLayoutX(boardTile.getLayoutX() + margin);
            this.boardScore.setLayoutY(boardTile.getLayoutY() + margin);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.gameGrid.widthProperty().addListener((observable, oldValue, newValue) -> this.gridSizeChanged());
        this.gameGrid.heightProperty().addListener((observable, oldValue, newValue) -> this.gridSizeChanged());

        this.gameOptionsMenu.getItems().addAll(new MenuItem("Geef op"));

        for (MenuItem item : this.gameOptionsMenu.getItems()) {
            item.setId(item.getText().toLowerCase());
            item.setOnAction(BoardController.this::gameOptionsMenuEventHandler);
        }
    }

    private void createNewPlayerBoard(int turn, List<BoardTile> boardTiles) {
        Connection connection = Singleton.getInstance().getConnection();
        boolean isPlayer1 = this.game.getUsernamePlayer1().equals(Singleton.getInstance().getUser().getUsername());

        try {
            StringBuilder boardPlayerQueryBuilder = new StringBuilder();

            boardPlayerQueryBuilder.append("INSERT INTO `");
            boardPlayerQueryBuilder.append(isPlayer1 ? "boardplayer1" : "boardplayer2");
            boardPlayerQueryBuilder.append("` (`game_id`, `username`, `turn_id`, `letter_id`, `tile_x`, `tile_y`) VALUES ");

            for (BoardTile boardTile : boardTiles) {
                Letter letter = boardTile.getLetterTile().getLetter();
                Board.Coordinate coordinate = this.board.getCoordinate(boardTile.getTile());
                boardPlayerQueryBuilder.append(String.format("(%s, '%s', %s, %s, %s, %s),",
                        this.game.getGameId(),
                        Singleton.getInstance().getUser().getUsername(),
                        turn,
                        letter.getLetterId(),
                        coordinate.getX(),
                        coordinate.getY()
                ));
            }
            boardPlayerQueryBuilder.setLength(boardPlayerQueryBuilder.length() - 1);
            boardPlayerQueryBuilder.append(";");

            PreparedStatement boardPlayerStatement = connection.prepareStatement(boardPlayerQueryBuilder.toString());

            boardPlayerStatement.executeUpdate();

            this.boardScore.setVisible(false);

            StringBuilder turnPlayerQueryBuilder = new StringBuilder();

            turnPlayerQueryBuilder.append("SELECT (`cp`.`score` + `cp`.`bonus`) as cp_score, `cp`.`turnaction_type` as cp_turntype, (`op`.`score` + `op`.`bonus`) as op_score, `op`.`turnaction_type` as op_turntype FROM `");
            turnPlayerQueryBuilder.append(isPlayer1 ? "turnplayer1" : "turnplayer2");
            turnPlayerQueryBuilder.append("` cp INNER JOIN `");
            turnPlayerQueryBuilder.append(isPlayer1 ? "turnplayer2" : "turnplayer1");
            turnPlayerQueryBuilder.append("`op ON `cp`.`game_id` = `op`.`game_id` AND `cp`.`turn_id` = `op`.`turn_id` WHERE `cp`.`game_id` = ? AND `cp`.`turn_id` = ?;");

            PreparedStatement turnPlayerStatement = connection.prepareStatement(turnPlayerQueryBuilder.toString());
            turnPlayerStatement.setInt(1, this.game.getGameId());
            turnPlayerStatement.setInt(2, turn);

            ResultSet turnPlayerResultSet = turnPlayerStatement.executeQuery();

            if (turnPlayerResultSet.next()) {
                Integer cpScore = NumberUtil.tryParse(turnPlayerResultSet.getString("cp_score"));
                String cpTurnType = turnPlayerResultSet.getString("cp_turntype");

                Integer opScore = NumberUtil.tryParse(turnPlayerResultSet.getString("op_score"));
                String opTurnType = turnPlayerResultSet.getString("op_turntype");

                boolean cpWon = true;
                if (opScore >= cpScore) {
                    cpWon = false;
                }
                if (cpTurnType.equals("pass")) {
                    cpWon = false;

                    if (cpTurnType.equals(opTurnType)) {
                        return;
                    }
                }

                String boardPlayerWon;
                if (isPlayer1 && cpWon) {
                    boardPlayerWon = "boardplayer1";
                } else if (isPlayer1) {
                    boardPlayerWon = "boardplayer2";
                } else if (cpWon) {
                    boardPlayerWon = "boardplayer2";
                } else {
                    boardPlayerWon = "boardplayer1";
                }

                StringBuilder turnBoardLetterQueryBuilder = new StringBuilder();
                turnBoardLetterQueryBuilder.append("INSERT INTO `turnboardletter` (`game_id`, `turn_id`, `letter_id`, `tile_x`, `tile_y`) SELECT `game_id`, `turn_id`, `letter_id`, `tile_x`, `tile_y` FROM `");
                turnBoardLetterQueryBuilder.append(boardPlayerWon);
                turnBoardLetterQueryBuilder.append("` WHERE `game_id` = ? AND `turn_id` = ?;");

                PreparedStatement turnBoardLetterStatement = connection.prepareStatement(turnBoardLetterQueryBuilder.toString());
                turnBoardLetterStatement.setInt(1, this.game.getGameId());
                turnBoardLetterStatement.setInt(2, turn);

                turnBoardLetterStatement.executeUpdate();

                this.createNewTurn(false);
            }
            this.loadAndRenderGame();
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    /**
     * Creates a new turnPlayer record in the database
     * If both player have player their turn, a new turn is created
     */
    private void createNewPlayerTurn(int turn) {
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
            turnPlayerStatement.setInt(2, turn);
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
            otherPlayerTurnStatement.setInt(2, turn);
            otherPlayerTurnStatement.setString(3, isPlayer1 ? this.game.getUsernamePlayer2() : this.game.getUsernamePlayer1());
            ResultSet otherPlayerTurnResultSet = otherPlayerTurnStatement.executeQuery();

            if (otherPlayerTurnResultSet.next()) {
                //If players have the same score.. the first player gets the bonus
                if (otherPlayerTurnResultSet.getInt("score") == points.getPoints()) {
                    String updateBonusQuery = "UPDATE `" + (isPlayer1 ? "turnplayer2" : "turnplayer1") + "` SET `bonus` = ? WHERE `game_id` = ? AND `turn_id` = ?;";
                    PreparedStatement updateBonusStatement = connection.prepareStatement(updateBonusQuery);
                    updateBonusStatement.setInt(1, 5);
                    updateBonusStatement.setInt(2, this.game.getGameId());
                    updateBonusStatement.setInt(3, turn);
                    updateBonusStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    /**
     * Creates a new turn in the database
     */
    private void createNewTurn(boolean isPassedTurn) {
        if (WordCrex.DEBUG_MODE) System.out.println("BoardController: Creating a new turn id");
        Connection connection = Singleton.getInstance().getConnection();
        try {
            int newTurnId = this.game.getCurrentTurn() + 1;

            PreparedStatement newTurnStatement = connection.prepareStatement("INSERT INTO `turn`(`game_id`, `turn_id`) VALUES (?,?)");
            newTurnStatement.setInt(1, this.game.getGameId());
            newTurnStatement.setInt(2, newTurnId);
            newTurnStatement.executeUpdate();

            this.handOutLetters(newTurnId, isPassedTurn);
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Nieuwe ronde al gestart");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
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
