package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.games.SuggestedAccounts;
import nl.avans.vsoprj2.wordcrex.controls.overview.GameItem;
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
    private List<String> list = new ArrayList<>();
    private String globalUserName = null;

    @FXML
    private VBox suggestedAccountsContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        suggestedAccountsContainer.managedProperty().bind(suggestedAccountsContainer.visibleProperty());

        this.loadAccounts();
    }

    private void loadAccounts() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `account` WHERE username != ?");
            statement.setString(1, "luc"); //TODO Add your username
            ResultSet resultSet = statement.executeQuery();

            suggestedAccountsContainer.setVisible(false);
            suggestedAccountsContainer.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                String userName = resultSet.getString("username");
                SuggestedAccounts suggestedAccounts = new SuggestedAccounts(userName);
                this.globalUserName = userName;
                suggestedAccounts.setOnSuggestedAccountsEvent(newGameClickEventHandler);
                list.add(userName);

                suggestedAccountsContainer.getChildren().add(suggestedAccounts);
                suggestedAccountsContainer.setVisible(true);
            }

            System.out.println(list);
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private EventHandler newGameClickEventHandler = new EventHandler() {
        @Override
        public void handle(Event event) {
            createNewGame(globalUserName);
        }
    };

    public void randomGameRequest() {
        Random rand = new Random();
        this.createGameRequest("NL", list.get(rand.nextInt(list.size())));
    }

    public void createNewGame(String username) {
        this.createGameRequest("NL", username);
    }

    private void createGameRequest(String letterset, String username2) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `game`(`game_state`, `letterset_code`, `username_player1`, `username_player2`, `answer_player2`, `username_winner`) VALUES ('request', ?, ?, ?, 'unknown', NULL)");
            statement.setString(1, letterset);
            statement.setString(2, "luc"); //TODO Add your username
            statement.setString(3, username2);

            statement.execute();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        } finally {
            this.navigateTo("/views/games.fxml");
            System.out.println("Created New game");
        }
    }
}
