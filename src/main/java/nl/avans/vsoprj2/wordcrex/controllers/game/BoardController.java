package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BoardController extends Controller {
    private Game game;
    public void confirmLettersButtonClicked() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Bevestig Woord");
        confirmationDialog.setHeaderText("Weet je zeker dat je dit woord wil spelen?");
        Optional<ButtonType> dialogResult = confirmationDialog.showAndWait();

        if(dialogResult.isPresent())
        if (dialogResult.get() == ButtonType.OK) {
            String word = "goed"; //TODO: Implement method from pull request : https://github.com/daanh432/Avans-VSOPRJ2/pull/51
            if(this.checkWord(word)) {
                //TODO: implement score calculations
                Alert alert = new Alert(Alert.AlertType.ERROR, "Goed woord.");
                alert.setTitle("Correcte dingen jongeman!");
                alert.showAndWait();
            } else {
                //Throws alert if word is not correct
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Dit is geen geldig woord.");
                invalidWordDialog.setTitle("Fout Woord");
                invalidWordDialog.showAndWait();
            }
        }
    }

    /**
     * This method needs to be called in the BeforeNavigation.
     * See following link : https://github.com/daanh432/Avans-VSOPRJ2/pull/35#discussion_r420678493
     *
     * @param game - The game id from the database to display.
     */
    public void setGame(Game game) {
        if (game == null) throw new IllegalArgumentException("Game can not be null");
        this.game = game;
    }

    public boolean checkWord(String word) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'accepted');");
            statement.setString(1, word);
            statement.setString(2, this.game.getLettersetCode());
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}