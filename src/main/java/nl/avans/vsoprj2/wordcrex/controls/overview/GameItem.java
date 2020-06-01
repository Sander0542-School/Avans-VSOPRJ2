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

    private Game game;

    public GameItem() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/overview/GameItem.fxml"));
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

        this.setGame(game);
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        if (game == null) throw new IllegalArgumentException("Game may not be null");
        this.game = game;
        final String currentUsername = Singleton.getInstance().getUser().getUsername();

        if (!currentUsername.equals(game.getUsernamePlayer1()) && !currentUsername.equals(game.getUsernamePlayer2())) {
            this.gameTitleLabel.setText(String.format("%s VS %s - %s", game.getUsernamePlayer1(), game.getUsernamePlayer2(), game.getLettersetCode()));
        } else {
            String otherUser = game.getUsernamePlayer1().equals(currentUsername) ? game.getUsernamePlayer2() : game.getUsernamePlayer1();
            this.gameTitleLabel.setText(String.format("%s - %s", otherUser, game.getLettersetCode()));
        }
        this.messageLabel.setText(game.getMessage());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
