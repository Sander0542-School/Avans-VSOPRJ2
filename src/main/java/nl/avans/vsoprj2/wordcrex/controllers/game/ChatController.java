package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gamechat.ChatRow;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.ChatMessage;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChatController extends Controller {
    private Game game;
    private List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

    @FXML
    private ScrollPane chatScrollContainer;
    @FXML
    private TextArea chatMessageInput;
    @FXML
    private VBox chatMessagesContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatMessageInput.addEventFilter(KeyEvent.KEY_PRESSED, this::sendMessageHandler);
        chatMessageInput.addEventFilter(KeyEvent.KEY_RELEASED, this::sendMessageHandler);
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
        this.fetch();
        this.render();
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

            while (resultSet.next()) {
                this.chatMessages.add(new ChatMessage(resultSet));
            }
        } catch (SQLException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "De berichten konden niet worden opgehaald.\nProbeer het later opnieuw.");
            errorAlert.setTitle("Chat Geschiedenis");
            errorAlert.showAndWait();
        }
    }

    /**
     * Collected instances of chat messages will be converted to displayable components and rendered appropriately.
     */
    private void render() {
        Account user = Singleton.getInstance().getUser();
        List<ChatRow> chatRows = chatMessages.stream().map(chatMessage -> new ChatRow(chatMessage, !user.getUsername().equals(chatMessage.getUsername()))).collect(Collectors.toList());
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
    private void deleteMessagesHandler() {
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
                    this.chatMessages.clear();
                    this.fetch();
                    this.render();
                } catch (SQLException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Iets is er fout gegaan bij het verwijderen van de berichten.\nProbeer het later opnieuw.");
                    errorAlert.setTitle("Alle berichten verwijderen");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    /**
     * If all conditions are met it will send and store a message in the database.
     *
     * @param keyEvent - KeyEvent send by Java FXML
     */
    private void sendMessageHandler(KeyEvent keyEvent) {
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
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het versturen van je bericht.\nProbeer het later opnieuw.");
                    errorAlert.setTitle("Versturen bericht");
                    errorAlert.showAndWait();
                }
            }
            keyEvent.consume();
        }
    }
}

