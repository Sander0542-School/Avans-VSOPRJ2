package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controllers.GamesController;
import nl.avans.vsoprj2.wordcrex.controls.information.DictionaryEntry;
import nl.avans.vsoprj2.wordcrex.controls.overview.GameItem;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Game;
import nl.avans.vsoprj2.wordcrex.models.Word;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DictionaryListController extends Controller {

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/information.fxml");
    }

    @FXML
    private Button acce;

    @FXML
    private VBox dictionaryEntryContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        this.dictionaryEntryContainer.managedProperty().bind(this.dictionaryEntryContainer.visibleProperty());
        this.PopulateWordList();
    }

    public DictionaryListController(){

    }

    private void PopulateWordList(){

        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `dictionary` WHERE `state` = ?;");
            statement.setString(1, "pending");

            ResultSet resultSet = statement.executeQuery();

            this.dictionaryEntryContainer.setVisible(false);
            this.dictionaryEntryContainer.getChildren().removeIf(node -> node instanceof GameItem);

            while (resultSet.next()) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(new Word(resultSet));
                this.dictionaryEntryContainer.getChildren().add(dictionaryEntry);
                this.dictionaryEntryContainer.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
