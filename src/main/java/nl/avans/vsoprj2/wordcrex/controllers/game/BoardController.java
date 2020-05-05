package nl.avans.vsoprj2.wordcrex.controllers.game;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoardController extends Controller {

    public boolean checkWord(String word, String letterSetCode) {
        Connection connection = Singleton.getInstance().getConnection();
        PreparedStatement statement;
        String query = "SELECT EXISTS(SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'accepted');";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,word);
            statement.setString(2,letterSetCode);
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
