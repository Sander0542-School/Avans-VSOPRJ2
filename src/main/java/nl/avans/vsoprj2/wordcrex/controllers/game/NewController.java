package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.games.SuggestedAccount;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Statistic;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class NewController extends Controller {
    private List<String> usernameslist = new ArrayList<>();

    @FXML
    private VBox suggestedAccountsContainer;

    @FXML
    private Label highScoreLabel;

    private Statistic statistic;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.suggestedAccountsContainer.managedProperty().bind(this.suggestedAccountsContainer.visibleProperty());

        this.loadHighScore();
        this.loadAccounts();
    }

    private void loadHighScore() {
        this.statistic = Singleton.getInstance().getUser().getStatistic();
        this.highScoreLabel.setText(this.statistic.getTopGameScore().toString());
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

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/games.fxml");
    }

    @FXML
    private void handleRequestAction() {
        Random rand = new Random();
        this.createGameRequest("NL", this.usernameslist.get(rand.nextInt(this.usernameslist.size())));
    }

    public void createNewGame(String otherPlayer) {
        this.createGameRequest("NL", otherPlayer);
    }

    private void createGameRequest(String letterset, String otherPlayer) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement checkAllowedStatement = connection.prepareStatement("SELECT COUNT(*) as count FROM `game` WHERE `game_state`='request' AND username_player1 = ? AND username_player2 = ? AND answer_player2='unknown';");
            checkAllowedStatement.setString(1, Singleton.getInstance().getUser().getUsername());
            checkAllowedStatement.setString(2, otherPlayer);
            ResultSet allowedResult = checkAllowedStatement.executeQuery();
            if (allowedResult.next() && allowedResult.getInt("count") != 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Je hebt al een openstaande uitdaging!");
                alert.setTitle("Dit mag niet.");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

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

                while (symbolResult.next()) {
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
        }

        this.navigateTo("/views/games.fxml");
    }
}
