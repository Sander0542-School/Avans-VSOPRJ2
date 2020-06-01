package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.ResourceBundle;

public class DictionaryController extends Controller {
    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/information.fxml");
    }

    @FXML
    private TextField username;
    @FXML
    private TextField word;
    @FXML
    private Label error;
    @FXML
    private ComboBox<String> language;

    private final Dictionary<String, String> languages = new Hashtable<String, String>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.language.getItems().removeAll(this.language.getItems());
        this.language.getItems().addAll(this.Languages());

        this.username.setText(Singleton.getInstance().getUser().getUsername());
        this.username.setDisable(true);
    }


    public DictionaryController() {
    }

    private String[] Languages() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM letterset");
            ResultSet result = statement.executeQuery();

            result.last();

            String[] returnValue = new String[result.getRow()];
            result.beforeFirst();

            while (result.next()) {
                returnValue[result.getRow() - 1] = result.getString(2);
                this.languages.put(result.getString(2), result.getString(1));
            }
            return returnValue;
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void getPending(String word, String code) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM dictionary WHERE word = ? AND letterset_code = ? AND state = 'pending'");
            statement.setString(1, word);
            statement.setString(2, code);
            ResultSet result = statement.executeQuery();
            while (result.next()) {

            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void submit() {
        //validation
        if (this.username.getText().trim().isEmpty()) {
            this.error.setVisible(true);
            this.error.setText("Username");
            return;
        }

        if (this.word.getText().trim().isEmpty()) {
            this.error.setVisible(true);
            this.error.setText("Word");
            return;
        }

        String selectedLanguage = this.language.getValue();
        if (selectedLanguage == null || this.languages.get(selectedLanguage) == null) {
            this.error.setVisible(true);
            this.error.setText("Language");
            return;
        }


        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM dictionary WHERE `word` = ? AND `letterset_code` = ?");
            statement.setString(1, this.word.getText().trim());
            statement.setString(2, this.languages.get(selectedLanguage));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                this.error.setVisible(true);
                this.error.setText("Woord is eerder toegevoegd");
                return;
            }

            statement = connection.prepareStatement("INSERT INTO dictionary (word, letterset_code, state, username) VALUES (?,?,'pending',?)");
            statement.setString(1, this.word.getText().trim());
            statement.setString(2, this.languages.get(selectedLanguage));
            statement.setString(3, this.username.getText().trim());
            int result = statement.executeUpdate();
            if (result > 0) {
                this.error.setVisible(true);
                this.error.setText("Verstuurd");
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

    }
}
