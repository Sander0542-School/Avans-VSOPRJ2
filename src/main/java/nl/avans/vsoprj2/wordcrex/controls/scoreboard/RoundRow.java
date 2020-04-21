package nl.avans.vsoprj2.wordcrex.controls.scoreboard;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoundRow extends AnchorPane implements Initializable {
    //TODO Make a model for this ugly code
    private String round;
    private String letterSet;
    private String playerOneName;
    private String playerTwoName;
    private String playerOneTime;
    private String playerTwoTime;
    private String playerOneWord;
    private String playerTwoWord;
    private String playerOneScore;
    private String playerTwoScore;

    public RoundRow() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/scoreboard/RoundRow.fxml"));
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

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getLetterSet() {
        return letterSet;
    }

    public void setLetterSet(String letterSet) {
        this.letterSet = letterSet;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public void setPlayerOneName(String playerOneName) {
        this.playerOneName = playerOneName;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public void setPlayerTwoName(String playerTwoName) {
        this.playerTwoName = playerTwoName;
    }

    public String getPlayerOneTime() {
        return playerOneTime;
    }

    public void setPlayerOneTime(String playerOneTime) {
        this.playerOneTime = playerOneTime;
    }

    public String getPlayerTwoTime() {
        return playerTwoTime;
    }

    public void setPlayerTwoTime(String playerTwoTime) {
        this.playerTwoTime = playerTwoTime;
    }

    public String getPlayerOneWord() {
        return playerOneWord;
    }

    public void setPlayerOneWord(String playerOneWord) {
        this.playerOneWord = playerOneWord;
    }

    public String getPlayerTwoWord() {
        return playerTwoWord;
    }

    public void setPlayerTwoWord(String playerTwoWord) {
        this.playerTwoWord = playerTwoWord;
    }

    public String getPlayerOneScore() {
        return playerOneScore;
    }

    public void setPlayerOneScore(String playerOneScore) {
        this.playerOneScore = playerOneScore;
    }

    public String getPlayerTwoScore() {
        return playerTwoScore;
    }

    public void setPlayerTwoScore(String playerTwoScore) {
        this.playerTwoScore = playerTwoScore;
    }
}
