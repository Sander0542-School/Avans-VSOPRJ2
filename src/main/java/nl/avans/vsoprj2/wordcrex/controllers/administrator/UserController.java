package nl.avans.vsoprj2.wordcrex.controllers.administrator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController extends Controller {
    //TODO Fix to work with multiple user roles
    @FXML
    public ComboBox userComboBox;

    @FXML
    public ComboBox userRoleComboBox;

    @FXML
    public Label currentUser;

    @FXML
    public Button changeUserRoleButton;

    @FXML
    public TextInputControl searchInput;

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.getAllUsers();
        this.getAllRoles();
    }

    /**
     * Get all the roles from the database and fills the comboBox
     */
    private void getAllRoles() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement usersStatement = connection.prepareStatement("SELECT `role` FROM `role`");
            ResultSet users = usersStatement.executeQuery();

            while (users.next()) {
                this.userRoleComboBox.getItems().add(users.getString("role"));
            }

        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de rollen.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }

    /**
     * Get all the users from the database and fills the comboBox
     */
    private void getAllUsers() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement usersStatement = connection.prepareStatement("SELECT a.username, ar.role FROM account a INNER JOIN accountrole ar ON a.username = ar.username ");
            ResultSet users = usersStatement.executeQuery();

            while (users.next()) {
                if(!users.getString("username").equals(Singleton.getInstance().getUser().getUsername())) {
                    Account account = new Account(users);
                    this.userComboBox.getItems().add(account);
                }
            }

        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de gebruikers.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }

    /**
     * Shows the form and fills the fields with the correct data
     *
     * @param account - The account that is going to be changed
     */
    private void handleFormChanges(Account account) {
        if(account != null) {
            this.currentUser.setText(account.getUsername());
            this.userRoleComboBox.getSelectionModel().select(account.getRole());
            this.currentUser.setVisible(true);
            this.userRoleComboBox.setVisible(true);
            this.changeUserRoleButton.setVisible(true);
        }
    }

    @FXML
    private void handleUserSelection() {
        this.searchInput.setText("");
        this.handleFormChanges((Account) this.userComboBox.getValue());
    }

    @FXML
    private void handleUserSearch() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            this.userComboBox.getSelectionModel().select(null);
            PreparedStatement userStatement = connection.prepareStatement("SELECT a.username, ar.role FROM account a INNER JOIN accountrole ar ON a.username = ar.username WHERE a.username=?");
            userStatement.setString(1, this.searchInput.getText().trim());
            ResultSet user = userStatement.executeQuery();

            if (user.next()) {
                Account account = new Account(user);
                this.handleFormChanges(account);
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.WARNING, "De gebruiker die je probeert te zoeken bestaat niet!");
                invalidWordDialog.setTitle("Pas op");
                invalidWordDialog.showAndWait();
            }
        } catch (SQLException ex) {
            if(WordCrex.DEBUG_MODE) {
                System.err.println(ex.getErrorCode());
            } else {
                Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de gebruiker.");
                invalidWordDialog.setTitle("Error");
                invalidWordDialog.showAndWait();
            }
        }
    }

    @FXML
    private void handleUserRoleChangeAction() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Gebruikersrol wijzigen");
        confirmationDialog.setHeaderText("Weet je zeker dat je de gebruikersrol van " + this.currentUser.getText() + " wil wijzgen?");
        Optional<ButtonType> dialogResult = confirmationDialog.showAndWait();

        if (dialogResult.isPresent()) {
            if (dialogResult.get() == ButtonType.OK) {
                Connection connection = Singleton.getInstance().getConnection();
                try {
                    PreparedStatement userStatement = connection.prepareStatement("UPDATE `accountrole` SET `role` = ? WHERE `username` = ?");
                    userStatement.setString(1, this.userRoleComboBox.getValue().toString());
                    userStatement.setString(2, this.currentUser.getText());
                    userStatement.executeUpdate();

                    //reset user combobox to get the new role of the changed player
                    this.userComboBox.getItems().clear();
                    this.getAllUsers();

                    //reset form
                    this.currentUser.setVisible(false);
                    this.userRoleComboBox.setVisible(false);
                    this.changeUserRoleButton.setVisible(false);
                    this.searchInput.setText("");

                } catch (SQLException ex) {
                    if(WordCrex.DEBUG_MODE) {
                        System.err.println(ex.getErrorCode());
                    } else {
                        Alert invalidWordDialog = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het veranderen van de gebruikersrol.");
                        invalidWordDialog.setTitle("Error");
                        invalidWordDialog.showAndWait();
                    }
                }
            }
        }
    }
}