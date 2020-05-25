package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DictionaryController extends Controller{

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
    private ComboBox language;



    public DictionaryController(){
        this.fillLanguageDropdown();
    }

    private void fillLanguageDropdown(){
        //SELECT * FROM `letterset`
    }

    public void getPending(){
        //SELECT * FROM `dictionary` WHERE `letterset_code` = ? AND state = 'pending'
    }

    public void submit(){
        //validation

        //Insert
        //INSERT INTO `dictionary`(`word`, `letterset_code`, `state`, `username`) VALUES ([value-1],[value-2],[value-3],[value-4])
    }
}
