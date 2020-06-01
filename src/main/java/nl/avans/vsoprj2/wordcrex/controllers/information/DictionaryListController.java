package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.information.DictionaryEntry;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Account;
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
        super.initialize(url, resourceBundle);
        this.dictionaryEntryContainer.managedProperty().bind(this.dictionaryEntryContainer.visibleProperty());

        this.PopulateWordList();
    }

    private void PopulateWordList() {
        Boolean isModerator = Singleton.getInstance().getUser().hasRole(Account.Role.MODERATOR);

        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement;
            if(isModerator ){
                statement = connection.prepareStatement("SELECT * FROM `dictionary` WHERE `state` = 'pending';");
            }else {
                statement = connection.prepareStatement("SELECT * FROM `dictionary` WHERE `username` = ?;");
                statement.setString(1, Singleton.getInstance().getUser().getUsername());
            }
            ResultSet resultSet = statement.executeQuery();

            this.dictionaryEntryContainer.setVisible(false);
            this.dictionaryEntryContainer.getChildren().removeIf(node -> node instanceof DictionaryEntry);

            while (resultSet.next()) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(new Word(resultSet));

                if(isModerator ){
                    dictionaryEntry.acceptButton.setOnMouseClicked(event -> DictionaryListController.this.ReviewWord(dictionaryEntry, true));
                    dictionaryEntry.denyButton.setOnMouseClicked(event -> DictionaryListController.this.ReviewWord(dictionaryEntry, false));
                }else {
                    dictionaryEntry.acceptButton.setVisible(false);
                    dictionaryEntry.denyButton.setVisible(false);
                }


                this.dictionaryEntryContainer.getChildren().add(dictionaryEntry);
                this.dictionaryEntryContainer.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void ReviewWord(DictionaryEntry dictionaryEntry, Boolean accept) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE `dictionary` SET `state` = ? WHERE `word` = ? AND `letterset_code` = ?;");
            statement.setString(1, accept ? "accepted" : "denied");
            statement.setString(2, dictionaryEntry.getWord());
            statement.setString(3, dictionaryEntry.getLanguage());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        this.PopulateWordList();
    }
}
