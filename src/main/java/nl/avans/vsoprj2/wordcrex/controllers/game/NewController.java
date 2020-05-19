package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.games.SuggestedAccount;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class NewController extends Controller {
    private List<String> usernameslist = new ArrayList<>();

    @FXML
    private VBox suggestedAccountsContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.suggestedAccountsContainer.managedProperty().bind(this.suggestedAccountsContainer.visibleProperty());

        this.loadAccounts();
    }

    private void loadAccounts() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM account WHERE username != ?");
            statement.setString(1, Singleton.getInstance().getUser().getUsername());
            ResultSet resultSet = statement.executeQuery();

            this.suggestedAccountsContainer.setVisible(false);
            this.suggestedAccountsContainer.getChildren().removeIf(node -> node instanceof SuggestedAccount);

            while (resultSet.next()) {
                final String username = resultSet.getString("username");
                this.usernameslist.add(username);

                SuggestedAccount suggestedAccount = new SuggestedAccount(username);
                suggestedAccount.setOnInviteEvent(event -> NewController.this.createNewGame(username));

                this.suggestedAccountsContainer.getChildren().add(suggestedAccount);
                this.suggestedAccountsContainer.setVisible(true);
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void handleRequestAction() {
        Random rand = new Random();
        this.createGameRequest("NL", this.usernameslist.get(rand.nextInt(this.usernameslist.size())));
    }

    public void createNewGame(String otherPlayer) {
        this.createGameRequest("NL", otherPlayer);
    }

    private void createGameRequest(String letterset, String otherPlayer) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO game(game_state, letterset_code, username_player1, username_player2, answer_player2, username_winner) VALUES ('request', ?, ?, ?, 'unknown', NULL)");
            statement.setString(1, letterset);
            statement.setString(2, Singleton.getInstance().getUser().getUsername());
            statement.setString(3, otherPlayer);

            statement.execute();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        } finally {
            this.navigateTo("/views/games.fxml");
        }
    }
}
