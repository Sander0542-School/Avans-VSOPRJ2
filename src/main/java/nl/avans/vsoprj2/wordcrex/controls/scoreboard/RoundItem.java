package nl.avans.vsoprj2.wordcrex.controls.scoreboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoundItem extends AnchorPane implements Initializable {
    @FXML
    private Label nameLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label wordLabel;

    @FXML
    private Label scoreLabel;


    public RoundItem() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/scoreboard/RoundItem.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public String getName() {
        return this.nameLabel.getText();
    }

    public void setName(String name) {
        this.nameLabel.setText(name);
    }

    public String getTime() {
        return this.timeLabel.getText();
    }

    public void setTime(String time) {
        this.timeLabel.setText(time);
    }

    public String getWord() {
        return this.wordLabel.getText();
    }

    public void setWord(String word) {
        this.wordLabel.setText(word);
    }

    public String getScore() {
        return this.scoreLabel.getText();
    }

    public void setScore(String score) {
        this.scoreLabel.setText(score);
    }
}
