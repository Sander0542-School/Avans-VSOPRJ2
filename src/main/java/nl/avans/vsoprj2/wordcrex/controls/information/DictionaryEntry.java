package nl.avans.vsoprj2.wordcrex.controls.information;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DictionaryEntry extends AnchorPane implements Initializable {

    @FXML
    private Label wordLabel;

    @FXML
    private Label languageLabel;

    @FXML
    private JButton acceptButton;

    @FXML
    private JButton denyButton;


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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
