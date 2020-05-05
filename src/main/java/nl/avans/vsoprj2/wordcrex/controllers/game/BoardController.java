package nl.avans.vsoprj2.wordcrex.controllers.game;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoardController extends Controller {

    public boolean checkWord(String word, String letterSet) {
        Connection connection = Singleton.getInstance().getConnection();
        PreparedStatement statement;
        String query = "SELECT EXISTS(SELECT * FROM dictionary WHERE word = '" + word + "' AND letterset_code = '" + letterSet + "' AND state = 'accepted');";
        try {
            statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
