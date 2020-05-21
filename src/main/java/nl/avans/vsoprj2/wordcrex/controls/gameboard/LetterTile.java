package nl.avans.vsoprj2.wordcrex.controls.gameboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nl.avans.vsoprj2.wordcrex.models.Letter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LetterTile extends AnchorPane implements Initializable {
    @FXML
    private Label worth;
    @FXML
    private Label letter;

    private Letter letterModel;

    public LetterTile(Character character, int worth) {
        this();

        this.letter.setText(character.toString());
        this.worth.setText(String.valueOf(worth));
    }

    public LetterTile() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/gameboard/LetterTile.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLetterModel(Letter letterModel){
        this.letterModel = letterModel;
    }

    public Letter getLetterModel(){
        return this.letterModel;
    }

    public void selectLetter(){
        this.setStyle("-fx-background-color: #A3A3A3; -fx-background-radius: 6;");
    }

    public void deselectLetter(){
        this.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 6;");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
