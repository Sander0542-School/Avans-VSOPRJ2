package nl.avans.vsoprj2.wordcrex.controls.gamechat;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import nl.avans.vsoprj2.wordcrex.models.ChatMessage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatRow extends HBox implements Initializable {

    @FXML
    private Label message;

    public ChatRow() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/gamechat/ChatRow.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatRow(ChatMessage chatMessage, boolean left) {
        this();
        this.setText(chatMessage.getMessage());
        this.setLeft(left);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public boolean getLeft() {
        return this.getAlignment().equals(Pos.CENTER_LEFT);
    }

    public void setLeft(boolean left) {
        if (left) {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(0, 80, 0, 0));
        } else {
            this.setAlignment(Pos.CENTER_RIGHT);
            this.setPadding(new Insets(0, 0, 0, 80));
        }
    }

    public String getText() {
        return this.message.getText();
    }

    public void setText(String text) {
        this.message.setText(text);
        this.message.setWrapText(true);
    }
}
