package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gamechat.ChatRow;
import nl.avans.vsoprj2.wordcrex.models.ChatMessage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChatController extends Controller implements Initializable {
    private List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

    @FXML
    private TextArea chatMessageInput;
    @FXML
    private VBox chatMessagesContainer;

    public ChatController() {
        Connection connection = Singleton.getInstance().getConnection();

        int gameId = 502; // TODO passing gameId to this class

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT cl.username, cl.game_id, cl.moment, cl.message FROM chatline cl WHERE cl.game_id = ?");
            statement.setInt(1, gameId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ChatMessage chatMessage = new ChatMessage(resultSet.getString("username"), resultSet.getDate("moment"), resultSet.getString("message"));
                chatMessages.add(chatMessage);
            }
        } catch (SQLException e) {
            //TODO Handle error
        }
    }

    private void sendMessageHandler(KeyEvent keyEvent) {
        String currentUserName = "jagermeester"; //TODO Convert to user model

        if (keyEvent.getCode() == KeyCode.ENTER && !keyEvent.isShiftDown()) {
            if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
                System.out.println("Enter pressed, TODO Send message");
            }
            keyEvent.consume();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String currentUserName = "jagermeester"; //TODO Convert to user model

        List<ChatRow> chatRows = chatMessages.stream().map(chatMessage -> new ChatRow(chatMessage.getMessage(), currentUserName.equals(chatMessage.getUsername()))).collect(Collectors.toList());

        chatMessagesContainer.getChildren().addAll(chatRows);

        chatMessageInput.addEventFilter(KeyEvent.KEY_PRESSED, this::sendMessageHandler);
        chatMessageInput.addEventFilter(KeyEvent.KEY_RELEASED, this::sendMessageHandler);
    }
}

