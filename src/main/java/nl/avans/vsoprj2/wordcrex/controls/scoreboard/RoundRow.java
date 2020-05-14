package nl.avans.vsoprj2.wordcrex.controls.scoreboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.avans.vsoprj2.wordcrex.models.ScoreboardRound;

import java.io.IOException;

public class RoundRow extends VBox {
    private ScoreboardRound scoreboardRound;

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

            this.player1Name.managedProperty().bind(this.player1Name.visibleProperty());
            this.player2Name.managedProperty().bind(this.player2Name.visibleProperty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RoundRow(ScoreboardRound scoreboardRound) {
        this();
        this.scoreboardRound = scoreboardRound;
        this.render();
    }

    public void render() {
        int totalScorePlayerOne = this.scoreboardRound.getScorePlayerOne() + this.scoreboardRound.getBonusPlayerOne();
        int totalScorePlayerTwo = this.scoreboardRound.getScorePlayerTwo() + this.scoreboardRound.getBonusPlayerTwo();
        this.round.setText(String.valueOf(this.scoreboardRound.getTurnId()));
        this.letterSet.setText(this.scoreboardRound.getHandContent());
        this.player1Name.setText(this.scoreboardRound.getUsernamePlayerOne());
        this.player2Name.setText(this.scoreboardRound.getUsernamePlayerTwo());
        this.player1Score.setText(String.valueOf(totalScorePlayerOne));
        this.player2Score.setText(String.valueOf(totalScorePlayerTwo));
        this.player1Word.setText(this.scoreboardRound.getWordPlayerOne());
        this.player2Word.setText(this.scoreboardRound.getWordPlayerTwo());
        this.player1Time.setText(""); // Time isn't present in the database?
        this.player2Time.setText(""); // Time isn't present in the database?

        if (totalScorePlayerOne > totalScorePlayerTwo) {
            this.player1Name.setFont(new Font(16.0));
            this.player2Name.setFont(new Font(14.0));
        } else {
            this.player1Name.setFont(new Font(14.0));
            this.player2Name.setFont(new Font(16.0));
        }
    }
}
