package nl.avans.vsoprj2.wordcrex.controllers.game;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbConnectionException;
import nl.avans.vsoprj2.wordcrex.models.Account;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BoardController extends Controller {
    private Game model;

    /**
     *
     * @param _model
     */
    public BoardController(Game _model)
    {
        model = _model;
    }

    /**
     *
     * @param winner
     */
    public void endGame(Account winner) {
        try {
            Connection connection = Singleton.getInstance().getConnection();

            String query = "UPDATE game SET game_state = ?, username_winner = ? WHERE game_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,model.getGameState().toString());
            preparedStatement.setString(2,winner.getUsername());
            preparedStatement.setInt(3,model.getGameId());
            preparedStatement.executeUpdate();

        } catch(Exception ex) {
            throw new DbConnectionException(ex);
        }
    }
}
