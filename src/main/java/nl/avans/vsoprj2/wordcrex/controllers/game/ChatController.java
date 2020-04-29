package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.gamechat.ChatRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatController extends Controller {
    private List<ChatRow> chatMessages = new ArrayList<ChatRow>();

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
                ChatRow chatRow = new ChatRow();
                chatRow.setText(resultSet.getString("message"));
                chatRow.setLeft(false); //TODO Check if username is same as current user (requires model te be finished for authentication)
                chatMessages.add(chatRow);
            }
        } catch (SQLException e) {
            //TODO Handle error
        }
    }

    public void initialize() {
        chatMessagesContainer.getChildren().addAll(chatMessages);
    }
}
