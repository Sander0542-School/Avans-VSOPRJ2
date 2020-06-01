package nl.avans.vsoprj2.wordcrex.controls.information;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nl.avans.vsoprj2.wordcrex.models.Game;
import nl.avans.vsoprj2.wordcrex.models.Word;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DictionaryEntry extends AnchorPane implements Initializable {

    @FXML
    private Label wordLabel;

    @FXML
    private Label languageLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button acceptButton;

    @FXML
    private Button denyButton;


    public DictionaryEntry() {
        super();
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/information/DictionaryEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public DictionaryEntry(Word word) {
        this();
        this.setWord(word);
    }

    public void setWord(Word word){
        this.wordLabel.setText(word.getWord());
        this.usernameLabel.setText(word.getUsername());
        this.languageLabel.setText(word.getLetterset_code());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
