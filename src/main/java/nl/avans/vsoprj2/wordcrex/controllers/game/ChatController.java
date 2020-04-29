package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gamechat.ChatRow;
import nl.avans.vsoprj2.wordcrex.models.ChatMessage;

import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChatController extends Controller implements Initializable {
    private int gameId = 502; // TODO passing gameId to this class

    private List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

    @FXML
    private ScrollPane chatScrollContainer;
    @FXML
    private TextArea chatMessageInput;
    @FXML
    private VBox chatMessagesContainer;

    public ChatController() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT cl.username, cl.game_id, cl.moment, cl.message FROM chatline cl WHERE cl.game_id = ? ORDER BY moment");
            statement.setInt(1, this.gameId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                chatMessages.add(new ChatMessage(resultSet.getString("username"), resultSet.getDate("moment"), resultSet.getString("message")));
            }
        } catch (SQLException e) {
            //TODO Handle error
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.update();
        chatMessageInput.addEventFilter(KeyEvent.KEY_PRESSED, this::sendMessageHandler);
        chatMessageInput.addEventFilter(KeyEvent.KEY_RELEASED, this::sendMessageHandler);
    }

    @FXML
    private void deleteButton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alle berichten verwijderen");
        alert.setContentText("Weet je zeker dat je alle berichten wilt verwijderen?");
        ButtonType okButton = new ButtonType("Ja", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Nee", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, noButton);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                //TODO Delete messages in database and arraylist
            }
        });
    }

    private void update() {
        String currentUserName = "jagermeester"; //TODO Convert to user model
        List<ChatRow> chatRows = chatMessages.stream().map(chatMessage -> new ChatRow(chatMessage.getMessage(), !currentUserName.equals(chatMessage.getUsername()))).collect(Collectors.toList());
        this.chatMessagesContainer.getChildren().clear();
        this.chatMessagesContainer.getChildren().addAll(chatRows);
        this.chatScrollContainer.applyCss();
        this.chatScrollContainer.layout();
        this.chatScrollContainer.setVvalue(1.0);
    }

    private void sendMessageHandler(KeyEvent keyEvent) {
        String currentUserName = "jagermeester"; //TODO Convert to user model

        if (keyEvent.getCode() == KeyCode.ENTER && !keyEvent.isShiftDown()) {
            if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
                ChatMessage chatMessage = new ChatMessage(currentUserName, Date.from(Instant.now()), this.chatMessageInput.getText());
                Connection connection = Singleton.getInstance().getConnection();

                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO chatline (username, game_id, message, moment) VALUES (?, ?, ?, ?)");
                    statement.setString(1, chatMessage.getUsername());
                    statement.setInt(2, this.gameId);
                    statement.setString(3, chatMessage.getMessage());
                    statement.setTimestamp(4, Timestamp.from(chatMessage.getDate().toInstant()));
                    statement.execute();
                } catch (SQLException e) {
                    //TODO Handle error
                } finally {
                    this.chatMessages.add(chatMessage);
                    this.update();
                    this.chatMessageInput.setText("");
                }
            }
            keyEvent.consume();
        }
    }
}

