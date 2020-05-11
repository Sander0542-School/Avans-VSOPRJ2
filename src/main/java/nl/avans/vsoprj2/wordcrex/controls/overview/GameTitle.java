package nl.avans.vsoprj2.wordcrex.controls.overview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameTitle extends AnchorPane implements Initializable {
    @FXML
    private Label titleLabel;

    public GameTitle() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/overview/GameTitle.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTitle() {
        return this.titleLabel.getText();
    }

    public void setTitle(String value) {
        this.titleLabel.setText(value);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
