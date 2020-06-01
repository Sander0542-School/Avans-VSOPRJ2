package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.controls.information.DictionaryEntry;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
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

        //While Debug_Mode is TRUE Singleton.getInstance().getUser().getRole() returns Null
        //Boolean isAdministrator = Singleton.getInstance().getUser().getRole().equals("administrator");

        Boolean isAdministrator = Singleton.getInstance().getUser().getRole() != null && Singleton.getInstance().getUser().getRole().equals("administrator");
        if (isAdministrator) {
            this.PopulateAdminWordList();
        } else {
            this.PopulateUserWordList();
        }
    }

    public DictionaryListController() {

    }

    private void PopulateUserWordList() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `dictionary` WHERE `username` = ?;");
            statement.setString(1, Singleton.getInstance().getUser().getUsername());

            ResultSet resultSet = statement.executeQuery();

            this.dictionaryEntryContainer.setVisible(false);
            this.dictionaryEntryContainer.getChildren().removeIf(node -> node instanceof DictionaryEntry);

            while (resultSet.next()) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(new Word(resultSet));

                dictionaryEntry.acceptButton.setVisible(false);
                dictionaryEntry.denyButton.setVisible(false);

                this.dictionaryEntryContainer.getChildren().add(dictionaryEntry);
                this.dictionaryEntryContainer.setVisible(true);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private void PopulateAdminWordList() {

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

    private void DenyWord(DictionaryEntry dictionaryEntry) {
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

        this.PopulateAdminWordList();
    }

    private void AcceptWord(DictionaryEntry dictionaryEntry) {
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

        this.PopulateAdminWordList();
    }
}
