package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DictionaryController extends Controller {
    @FXML
    private TextField username;
    @FXML
    private TextField word;
    @FXML
    private Label error;
    @FXML
    private ComboBox<String> languagesBox;

    private HashMap<String, String> languages;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.languages = this.getLanguages();
        this.languagesBox.getItems().addAll(this.languages.keySet());
        this.username.setText(Singleton.getInstance().getUser().getUsername());
    }

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/information.fxml");
    }

    @FXML
    private void handleListButton() {
        this.navigateTo("/views/information/dictionaryList.fxml");
    }

    private HashMap<String, String> getLanguages() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `code`, `description` FROM `letterset`");
            ResultSet result = statement.executeQuery();

            HashMap<String, String> languages = new HashMap<>();

            while (result.next()) {
                languages.put(result.getString("description"), result.getString("code"));
            }

            return languages;
        } catch (SQLException e) {
            WordCrex.handleException(e);

            throw new DbLoadException(e);
        }
    }

    public void submit() {
        if (this.word.getText().trim().isEmpty()) {
            this.showError("Voer een woord in");
            return;
        }

        String selectedLanguage = this.languagesBox.getValue();

        if (selectedLanguage == null || this.languages.get(selectedLanguage) == null) {
            this.showError("Selecteer een taal");
            return;
        }

        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement selectWord = connection.prepareStatement("SELECT * FROM `dictionary` WHERE `word` = ? AND `letterset_code` = ?");
            selectWord.setString(1, this.word.getText().trim().toLowerCase());
            selectWord.setString(2, this.languages.get(selectedLanguage));

            ResultSet resultSet = selectWord.executeQuery();
            if (resultSet.next()) {
                this.showError("Dit woord zit al in ons woordenboek!");
                return;
            }

            PreparedStatement insertWord = connection.prepareStatement("INSERT INTO `dictionary` (`word`, `letterset_code`, `state`, `username`) VALUES (?, ?, 'pending', ?);");
            insertWord.setString(1, this.word.getText().trim().toLowerCase());
            insertWord.setString(2, this.languages.get(selectedLanguage));
            insertWord.setString(3, Singleton.getInstance().getUser().getUsername());

            int result = insertWord.executeUpdate();

            if (result > 0) {
                this.showError("Inzending verstuurd");
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Oei. Het woord kon niet worden toegevoegd aan de database. Probeer het opnieuw!");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
        }
    }

    private void showError(String message) {
        this.error.setText(message);
        this.error.setVisible(true);
    }
}
