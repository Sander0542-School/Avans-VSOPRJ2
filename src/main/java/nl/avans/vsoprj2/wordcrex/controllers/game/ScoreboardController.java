package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.scoreboard.RoundRow;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;
import nl.avans.vsoprj2.wordcrex.models.ScoreboardRound;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ScoreboardController extends Controller {
    private Game game;
    private List<ScoreboardRound> scoreboardRounds = new ArrayList<>();

    @FXML
    public Label usernamePlayerOne;
    @FXML
    public Label usernamePlayerTwo;
    @FXML
    private VBox roundRowContainer;

    /**
     * Sets the game id and fetches + renders it.
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - The game from the database to display.
     */
    public void setGame(Game game) {
        if (game == null) throw new IllegalArgumentException("Game may not be null");
        this.game = game;
        this.fetch();
        this.render();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //region TODO Remove this region when testing is finished.
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM game WHERE game_id = 502");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                this.setGame(new Game(resultSet));
            }

            PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM account WHERE username='luc';");
            ResultSet resultSet1 = statement1.executeQuery();
            while (resultSet1.next()) {
                Singleton.getInstance().setUser(new Account(resultSet1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //endregion
    }

    /**
     * Fetches and converts SQL rows to ScoreboardRound class instances
     */
    private void fetch() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `t`.`game_id`," +
                    "`t`.`turn_id`," +
                    "`h`.`inhoud` as hand_inhoud," +
                    "`tp1`.`username_player1`," +
                    "`gp1`.`woorddeel` as woorddeel1," +
                    "`tp1`.`bonus`     as bonus1," +
                    "`tp1`.`score`     as score1," +
                    "(SELECT SUM(`score`) + SUM(`bonus`) FROM `turnplayer1` WHERE `game_id` = `t`.`game_id` AND `turn_id` <= `t`.turn_id) as totaal_score1," +
                    "`tp1`.`turnaction_type` as turntype1," +
                    "`tp2`.`username_player2`," +
                    "`gp2`.`woorddeel` as woorddeel2," +
                    "`tp2`.`bonus`     as bonus2," +
                    "`tp2`.`score`     as score2," +
                    "(SELECT SUM(`score`) + SUM(`bonus`) FROM `turnplayer2` WHERE `game_id` = `t`.`game_id` AND `turn_id` <= `t`.turn_id) as totaal_score2," +
                    "`tp2`.`turnaction_type` as turntype2 " +
                    "FROM `turn` t " +
                    "INNER JOIN `hand` h ON `t`.`game_id` = `h`.`game_id` AND `t`.`turn_id` = `h`.`turn_id` " +
                    "INNER JOIN `turnplayer1` tp1 ON `tp1`.`game_id` = `t`.`game_id` AND `tp1`.`turn_id` = `t`.`turn_id` " +
                    "INNER JOIN `gelegdplayer1` gp1 ON `gp1`.`game_id` = `tp1`.`game_id` AND `gp1`.`turn_id` = `tp1`.`turn_id` " +
                    "INNER JOIN `turnplayer2` tp2 ON `tp2`.`game_id` = `t`.`game_id` AND `tp2`.`turn_id` = `t`.`turn_id` " +
                    "INNER JOIN `gelegdplayer2` gp2 ON `gp2`.`game_id` = `tp2`.`game_id` AND `gp2`.`turn_id` = `tp2`.`turn_id` " +
                    "WHERE `t`.`game_id` = ? " +
                    "ORDER BY `t`.`turn_id` DESC");

            statement.setInt(1, this.game.getGameId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                this.scoreboardRounds.add(new ScoreboardRound(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Collected instances of scoreboard rounds will be converted to displayable components and rendered appropriately.
     */
    private void render() {
        List<RoundRow> roundRows = this.scoreboardRounds.stream().map(RoundRow::new).collect(Collectors.toList());
        this.roundRowContainer.getChildren().clear();
        this.roundRowContainer.getChildren().addAll(roundRows);
        this.usernamePlayerOne.setText(this.game.getUsernamePlayer1());
        this.usernamePlayerTwo.setText(this.game.getUsernamePlayer2());
    }

    @FXML
    private void backToGameScreen() {
        this.navigateTo("/views/game/board.fxml", new NavigationListener() {
            @Override
            public void beforeNavigate(Controller controller) {
                BoardController boardController = (BoardController) controller;
                boardController.setGame(ScoreboardController.this.game);
            }

            @Override
            public void afterNavigate(Controller controller) {

            }
        });
    }
}
