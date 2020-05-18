package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
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
    private List<String> userNamesList = new ArrayList<>();

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
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM account WHERE username != ?");
            statement.setString(1, Singleton.getInstance().getUser().getUsername());
            ResultSet resultSet = statement.executeQuery();

            this.suggestedAccountsContainer.setVisible(false);
            this.suggestedAccountsContainer.getChildren().removeIf(node -> node instanceof SuggestedAccount);

            while (resultSet.next()) {
                String userName = resultSet.getString("username");
                this.userNamesList.add(userName);

                final SuggestedAccount suggestedAccount = new SuggestedAccount(userName);
                suggestedAccount.setOnInviteEvent(event -> NewController.this.createNewGame(suggestedAccount.getUserName()));

                this.suggestedAccountsContainer.getChildren().add(suggestedAccount);
                this.suggestedAccountsContainer.setVisible(true);
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void handleRequestAction() {
        Random rand = new Random();
        this.createGameRequest("NL", this.userNamesList.get(rand.nextInt(this.userNamesList.size())));
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
