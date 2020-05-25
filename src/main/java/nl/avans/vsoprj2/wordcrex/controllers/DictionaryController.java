package nl.avans.vsoprj2.wordcrex.controllers;

public class DictionaryController extends Controller{

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
