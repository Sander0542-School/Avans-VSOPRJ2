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
    private VBox dictionaryEntryContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String userRole = Singleton.getInstance().getUser().getRole(); // userRole.equals("administrator")

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
            this.dictionaryEntryContainer.getChildren().removeIf(node -> node instanceof DictionaryEntry);

            while (resultSet.next()) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(new Word(resultSet));

                dictionaryEntry.acceptButton.setOnMouseClicked(event -> DictionaryListController.this.AcceptWord(dictionaryEntry));
                dictionaryEntry.denyButton.setOnMouseClicked(event -> DictionaryListController.this.DenyWord(dictionaryEntry));

                this.dictionaryEntryContainer.getChildren().add(dictionaryEntry);
                this.dictionaryEntryContainer.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void DenyWord(DictionaryEntry dictionaryEntry){
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement newPasswordStatement = connection.prepareStatement("UPDATE `dictionary` SET `state` = ? WHERE `word` = ? AND `letterset_code` = ?;");
            newPasswordStatement.setString(1, "denied");
            newPasswordStatement.setString(2, dictionaryEntry.getWord());
            newPasswordStatement.setString(3, dictionaryEntry.getLanguage());
            newPasswordStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        this.PopulateWordList();
    }

    private void AcceptWord(DictionaryEntry dictionaryEntry){
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement newPasswordStatement = connection.prepareStatement("UPDATE `dictionary` SET `state` = ? WHERE `word` = ? AND `letterset_code` = ?;");
            newPasswordStatement.setString(1, "accepted");
            newPasswordStatement.setString(2, dictionaryEntry.getWord());
            newPasswordStatement.setString(3, dictionaryEntry.getLanguage());
            newPasswordStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        this.PopulateWordList();
    }
}
