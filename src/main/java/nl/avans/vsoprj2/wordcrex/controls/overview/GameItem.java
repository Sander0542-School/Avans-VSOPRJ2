package nl.avans.vsoprj2.wordcrex.controls.overview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameItem extends AnchorPane implements Initializable {
    @FXML
    private Label gameTitleLabel;

    @FXML
    private Label messageLabel;

    public GameItem() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/overview/GameItem.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GameItem(Game game) {
        this();

        setGame(game);
    }

    public void setGame(Game game) {
        String otherUser = game.getUsernamePlayer1().equals(Singleton.getInstance().getUser()) ? game.getUsernamePlayer2() : game.getUsernamePlayer1(); //TODO(getUser() --> getUser().getUsername())

        gameTitleLabel.setText(String.format("%s - %s", otherUser, game.getLettersetCode()));
        messageLabel.setText(game.getMessage());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
