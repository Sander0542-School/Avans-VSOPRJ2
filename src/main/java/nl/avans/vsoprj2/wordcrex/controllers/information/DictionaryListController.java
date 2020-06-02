package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    private VBox dictionaryEntryContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        this.dictionaryEntryContainer.managedProperty().bind(this.dictionaryEntryContainer.visibleProperty());

        this.loadWords();
    }

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/information.fxml");
    }

    private void loadWords() {
        boolean isModerator = Singleton.getInstance().getUser().hasRole(Account.Role.MODERATOR);

        Connection connection = Singleton.getInstance().getConnection();

        try {
            String query = "SELECT `word`, `letterset_code`, `state`, `username` FROM `dictionary` WHERE `username` = ?";
            if (isModerator) {
                query += " OR `state` = 'pending';";
            }

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            this.dictionaryEntryContainer.setVisible(false);
            this.dictionaryEntryContainer.getChildren().removeIf(node -> node instanceof DictionaryEntry);

            while (resultSet.next()) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(new Word(resultSet));

                if (isModerator) {
                    dictionaryEntry.acceptButton.setOnMouseClicked(event -> DictionaryListController.this.reviewWord(dictionaryEntry, true));
                    dictionaryEntry.denyButton.setOnMouseClicked(event -> DictionaryListController.this.reviewWord(dictionaryEntry, false));
                } else {
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

    private void reviewWord(DictionaryEntry dictionaryEntry, boolean accept) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE `dictionary` SET `state` = ? WHERE `word` = ? AND `letterset_code` = ?;");
            statement.setString(1, accept ? "accepted" : "denied");
            statement.setString(2, dictionaryEntry.getWord());
            statement.setString(3, dictionaryEntry.getLanguage());

            if (statement.executeUpdate() > 0) {
                this.loadWords();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, String.format("Het woord %s kon niet worden %s", dictionaryEntry.getWord(), accept ? "geaccepteerd" : "afgekeurd"));
                alert.setTitle("Er is een fout opgetreden");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

    }
}
