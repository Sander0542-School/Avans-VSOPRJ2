package nl.avans.vsoprj2.wordcrex.controllers.game;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import javax.swing.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class NewController extends Controller {
    //todo 
    //private list allnames

    @FXML
    private VBox suggestedAccounts;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        suggestedAccounts.managedProperty().bind(suggestedAccounts.visibleProperty());

        loadAccounts("luc"); //TODO Add your username
    }

    private void loadAccounts(String Username) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `account` WHERE username != 'luc'");
            ResultSet resultSet = statement.executeQuery();

            System.out.println(resultSet);

            //Singleton.getInstance().getUser();
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void randomGameRequest() {
        this.createGame("NL", "Lars");
    }

    public void createNewGame() {
        this.createGame("NL", "Lars");
    }

    private void createGame(String letterset, String username2) {
        Boolean gameCreated = false;
    }
}
