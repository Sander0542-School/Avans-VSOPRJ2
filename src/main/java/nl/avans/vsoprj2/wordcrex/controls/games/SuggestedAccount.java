package nl.avans.vsoprj2.wordcrex.controls.games;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SuggestedAccount extends HBox implements Initializable {
    @FXML
    private Label usernameLabel;
    @FXML
    private Button inviteButton;
    private String userName;

    private EventHandler SuggestedAccountsEventHandler = null;

    public SuggestedAccount() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/games/SuggestedAccount.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserName() {
        return this.userName;
    }

    public SuggestedAccount(String account) {
        this();
        this.userName = account;
        this.usernameLabel.setText(account);
    }

    public void SuggestedAccountsClicked(MouseEvent event) {
        if (this.SuggestedAccountsEventHandler != null) {
            this.SuggestedAccountsEventHandler.handle(event);
        }
    }

    public void setOnSuggestedAccountsEvent(EventHandler eventHandler) {
        this.SuggestedAccountsEventHandler = eventHandler;
    }

    public EventHandler getOnSuggestedAccountsEvent() {
        return this.SuggestedAccountsEventHandler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
