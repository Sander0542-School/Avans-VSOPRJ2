package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.games.SuggestedAccount;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.net.URL;
import java.sql.*;
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
            PreparedStatement gameStatement = connection.prepareStatement("INSERT INTO game(game_state, letterset_code, username_player1, username_player2, answer_player2, username_winner) VALUES ('request', ?, ?, ?, 'unknown', NULL)", Statement.RETURN_GENERATED_KEYS);
            gameStatement.setString(1, letterset);
            gameStatement.setString(2, Singleton.getInstance().getUser().getUsername());
            gameStatement.setString(3, otherPlayer);

            gameStatement.executeUpdate();

            ResultSet resultSet = gameStatement.getGeneratedKeys();

            if (resultSet.next()) {
                int gameId = resultSet.getInt(1);

                PreparedStatement symbolsStatement = connection.prepareStatement("SELECT symbol, counted FROM symbol WHERE letterset_code = ?");
                symbolsStatement.setString(1, letterset);

                ResultSet symbolResult = symbolsStatement.executeQuery();

                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("INSERT INTO letter (letter_id, game_id, symbol_letterset_code, symbol) VALUES ");

                int letterId = 0;

                while(symbolResult.next()) {
                    String symbol = symbolResult.getString("symbol");
                    int counted = symbolResult.getInt("counted");

                    for (int i = 0; i < counted; i++) {
                        letterId++;
                        queryBuilder.append(String.format("(%s, %s, '%s', '%s'),", letterId, gameId, letterset, symbol));
                    }
                }
                queryBuilder.setLength(queryBuilder.length() - 1);

                queryBuilder.append(";");

                PreparedStatement lettersStatement = connection.prepareStatement(queryBuilder.toString());
                lettersStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        } finally {
            this.navigateTo("/views/games.fxml");
        }
    }
}
