package nl.avans.vsoprj2.wordcrex.controls.overview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameItem extends AnchorPane implements Initializable {
    @FXML
    private Label gameTitleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Label timeLeftLabel;

    public GameItem() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/overview/GameItem.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTitle() {
        return gameTitleLabel.getText();
    }

    public void setTitle(String value) {
        gameTitleLabel.setText(value);
    }

    public String getMessage() {
        return messageLabel.getText();
    }

    public void setMessage(String value) {
        messageLabel.setText(value);
    }

    public String getTimeLeft() {
        return messageLabel.getText();
    }

    public void setTimeLeft(String value) {
        timeLeftLabel.setText(value);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
