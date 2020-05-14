package nl.avans.vsoprj2.wordcrex.controls.scoreboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import nl.avans.vsoprj2.wordcrex.models.ScoreboardRound;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoundRow extends VBox implements Initializable {
    @FXML
    private Text round;
    @FXML
    private Text letterSet;

    @FXML
    private Label player1Name;
    @FXML
    private Label player1Time;
    @FXML
    private Label player1Word;
    @FXML
    private Label player1Score;

    @FXML
    private Label player2Name;
    @FXML
    private Label player2Time;
    @FXML
    private Label player2Word;
    @FXML
    private Label player2Score;

    public RoundRow() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/scoreboard/RoundRow.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RoundRow(ScoreboardRound scoreboardRound) {
        this();
        int totalScorePlayerOne = scoreboardRound.getScorePlayerOne() + scoreboardRound.getBonusPlayerOne();
        int totalScorePlayerTwo = scoreboardRound.getScorePlayerTwo() + scoreboardRound.getBonusPlayerTwo();
        this.player1Name.setText(scoreboardRound.getUsernamePlayerOne());
        this.player2Name.setText(scoreboardRound.getUsernamePlayerTwo());
        this.player1Score.setText(String.valueOf(totalScorePlayerOne));
        this.player2Score.setText(String.valueOf(totalScorePlayerTwo));
        this.player1Word.setText(scoreboardRound.getWordPlayerOne());
        this.player2Word.setText(scoreboardRound.getWordPlayerTwo());
        this.player1Time.setText(""); // Time isn't present in the database?
        this.player2Time.setText(""); // Time isn't present in the database?

        if (totalScorePlayerOne > totalScorePlayerTwo) {
            this.player1Name.setStyle("-fx-font-weight: bold");
            this.player2Name.setStyle("-fx-font-weight: normal");
        } else {
            this.player1Name.setStyle("-fx-font-weight: normal");
            this.player2Name.setStyle("-fx-font-weight: bold");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
