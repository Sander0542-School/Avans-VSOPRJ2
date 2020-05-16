package nl.avans.vsoprj2.wordcrex.controllers.game;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoardController extends Controller {
    private Game game;

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - Game model
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * @param winner - Account model
     */
    public void endGame(Account winner) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game SET game_state = ?, username_winner = ? WHERE game_id = ?");
            preparedStatement.setString(1, this.game.getGameState().toString());
            preparedStatement.setString(2, winner.getUsername());
            preparedStatement.setInt(3, this.game.getGameId());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new DbLoadException(e);
        }
    }

    public boolean checkWord(Game game, String word) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'accepted');");
            statement.setString(1, word);
            statement.setString(2, game.getLettersetCode());
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
