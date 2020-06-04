package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gamechat.ChatRow;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.ChatMessage;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ChatController extends Controller {
    private Game game;
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final Timer autoFetch = new Timer();

    @FXML
    private ScrollPane chatScrollContainer;
    @FXML
    private TextArea chatMessageInput;
    @FXML
    private VBox chatMessagesContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.chatMessageInput.addEventFilter(KeyEvent.KEY_PRESSED, this::handleSendMessage);
        this.chatMessageInput.addEventFilter(KeyEvent.KEY_RELEASED, this::handleSendMessage);
    }

    /**
     * Sets the game id and fetches + renders it.
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - The game id from the database to display.
     */
    public void setGame(Game game) {
        if (game == null) throw new IllegalArgumentException("Game may not be null");
        this.game = game;
        this.chatMessageInput.setVisible(this.game.getOwnGame());
        this.fetch();
        this.render();
        this.autoFetch.scheduleAtFixedRate(this.createTimerTask(), 5000, 5000);
    }

    /**
     * Creates a TimerTask to automatically fetch and rerender chat messages if the data is changed.
     *
     * @return TimerTask
     */
    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (WordCrex.DEBUG_MODE) System.out.println("ChatController: Autofetch running.");
                int originalSize = ChatController.this.chatMessages.size();
                ChatController.this.fetch();
                if (ChatController.this.chatMessages.size() != originalSize) {
                    if (WordCrex.DEBUG_MODE) System.out.println("ChatController: Autofetch data updated rendering.");
                    Platform.runLater(ChatController.this::render);
                }
            }
        };
    }

    /**
     * Fetches and converts SQL rows to ChatMessage class instances
     */
    private void fetch() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT cl.username, cl.game_id, cl.moment, cl.message FROM chatline cl WHERE cl.game_id = ? ORDER BY moment");
            statement.setInt(1, this.game.getGameId());
            ResultSet resultSet = statement.executeQuery();

            this.chatMessages.clear();

            while (resultSet.next()) {
                this.chatMessages.add(new ChatMessage(resultSet));
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "De berichten konden niet worden opgehaald.\nProbeer het later opnieuw.");
            errorAlert.setTitle("Chat Geschiedenis");
            errorAlert.showAndWait();
            try {
                this.navigateBackToGame();
            } catch (Exception ignore) {
                // Ignore exception
            }
        }
    }

    /**
     * Collected instances of chat messages will be converted to displayable components and rendered appropriately.
     */
    private void render() {
        Account user = Singleton.getInstance().getUser();
        List<ChatRow> chatRows = this.chatMessages.stream().map(chatMessage -> new ChatRow(chatMessage, !user.getUsername().equals(chatMessage.getUsername()))).collect(Collectors.toList());
        this.chatMessagesContainer.getChildren().clear();
        this.chatMessagesContainer.getChildren().addAll(chatRows);
        this.chatScrollContainer.applyCss();
        this.chatScrollContainer.layout();
        this.chatScrollContainer.setVvalue(1.0);
    }

    /**
     * Handles the delete button event to remove all messages for this game.
     */
    @FXML
    private void handleDeleteMessage() {
        if (!this.game.getOwnGame()) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alle berichten verwijderen");
        alert.setContentText("Weet je zeker dat je alle berichten wilt verwijderen?");
        ButtonType okButton = new ButtonType("Ja", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Nee", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, noButton);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                Connection connection = Singleton.getInstance().getConnection();
                try {
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM chatline WHERE game_id = ?");
                    statement.setInt(1, this.game.getGameId());
                    statement.execute();
                    this.fetch();
                    this.render();
                } catch (SQLException e) {
                    WordCrex.handleException(e);

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets foutgegaan bij het verwijderen van de berichten.\nProbeer het later opnieuw.");
                    errorAlert.setTitle("Alle berichten verwijderen");
                    errorAlert.showAndWait();
                    this.navigateBackToGame();
                }
            }
        });
    }

    /**
     * If all conditions are met it will send and store a message in the database.
     *
     * @param keyEvent - KeyEvent send by Java FXML
     */
    private void handleSendMessage(KeyEvent keyEvent) {
        if (!this.game.getOwnGame()) return;
        Account user = Singleton.getInstance().getUser();
        String messageContent = this.chatMessageInput.getText().trim();

        if (keyEvent.getCode() == KeyCode.ENTER && !keyEvent.isShiftDown()) {
            if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED && messageContent.length() > 0) {
                Connection connection = Singleton.getInstance().getConnection();

                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO chatline (username, game_id, message, moment) VALUES (?, ?, ?, ?)");
                    statement.setString(1, user.getUsername());
                    statement.setInt(2, this.game.getGameId());
                    statement.setString(3, messageContent);
                    statement.setTimestamp(4, Timestamp.from(Instant.now()));
                    statement.execute();
                    this.fetch();
                    this.render();
                    this.chatMessageInput.setText("");
                } catch (SQLException e) {
                    WordCrex.handleException(e);

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het versturen van je bericht.\nProbeer het later opnieuw.");
                    errorAlert.setTitle("Versturen bericht");
                    errorAlert.showAndWait();
                }
            }
            keyEvent.consume();
        }
    }

    @FXML
    private void navigateBackToGame() {
        this.autoFetch.cancel();
        this.autoFetch.purge();
        this.navigateTo("/views/game/board.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                BoardController boardController = (BoardController) controller;
                boardController.setGame(ChatController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }
}