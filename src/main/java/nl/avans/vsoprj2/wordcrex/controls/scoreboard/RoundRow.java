package nl.avans.vsoprj2.wordcrex.controls.scoreboard;

import javafx.fxml.FXML;
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

    @FXML
    private RoundItem playerOne;

    @FXML
    private RoundItem playerTwo;

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
        return this.playerOne.getName();
    }

    public void setPlayerOneName(String playerOneName) {
        this.playerOne.setName(playerOneName);
    }

    public String getPlayerTwoName() {
        return this.playerTwo.getName();
    }

    public void setPlayerTwoName(String playerTwoName) {
        this.playerTwo.setName(playerTwoName);
    }

    public String getPlayerOneTime() {
        return this.playerOne.getTime();
    }

    public void setPlayerOneTime(String playerOneTime) {
        this.playerOne.setTime(playerOneTime);
    }

    public String getPlayerTwoTime() {
        return this.playerTwo.getTime();
    }

    public void setPlayerTwoTime(String playerTwoTime) {
        this.playerTwo.setTime(playerTwoTime);
    }

    public String getPlayerOneWord() {
        return this.playerOne.getWord();
    }

    public void setPlayerOneWord(String playerOneWord) {
        this.playerOne.setWord(playerOneWord);
    }

    public String getPlayerTwoWord() {
        return this.playerTwo.getWord();
    }

    public void setPlayerTwoWord(String playerTwoWord) {
        this.playerTwo.setWord(playerTwoWord);
    }

    public String getPlayerOneScore() {
        return this.playerOne.getScore();
    }

    public void setPlayerOneScore(String playerOneScore) {
        this.playerOne.setScore(playerOneScore);
    }

    public String getPlayerTwoScore() {
        return this.playerTwo.getScore();
    }

    public void setPlayerTwoScore(String playerTwoScore) {
        playerTwo.setScore(playerTwoScore);
    }
}
