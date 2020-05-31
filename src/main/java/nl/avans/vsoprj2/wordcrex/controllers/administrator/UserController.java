package nl.avans.vsoprj2.wordcrex.controllers.administrator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UserController extends Controller {
    @FXML
    public VBox userContainer;

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.getAllUsers();
    }

    private void getAllUsers() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement usersStatement = connection.prepareStatement("SELECT a.username, ar.role FROM account a INNER JOIN accountrole ar ON a.username = ar.username ");
            ResultSet users = usersStatement.executeQuery();

            while (users.next()) {
                //TODO create user item
            }
        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan tijdens het wijzigen van je wachtwoord.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }
}