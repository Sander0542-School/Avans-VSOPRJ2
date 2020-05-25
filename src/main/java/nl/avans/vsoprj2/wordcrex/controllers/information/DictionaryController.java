package nl.avans.vsoprj2.wordcrex.controllers.information;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.Tile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    private TextField comment;
    @FXML
    private RadioButton add;
    @FXML
    private RadioButton delete;
    @FXML
    private ComboBox<String> language;



    public DictionaryController(){
        this.fillLanguageDropdown();
    }

    private void fillLanguageDropdown(){
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM letterset"); //returns 4 results instead of expected 2

            //this.language.getItems().clear();
            while (result.next()) {
                //this.language.getItems().add(result.getString(2));
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void getPending(String word, String code){
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

    public void submit(){
        //validation

        //Insert
        //INSERT INTO `dictionary`(`word`, `letterset_code`, `state`, `username`) VALUES ([value-1],[value-2],[value-3],[value-4])
    }
}
