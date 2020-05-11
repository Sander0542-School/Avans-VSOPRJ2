package nl.avans.vsoprj2.wordcrex.controls.scoreboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
