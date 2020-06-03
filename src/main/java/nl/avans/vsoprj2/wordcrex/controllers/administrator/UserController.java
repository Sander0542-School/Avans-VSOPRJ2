package nl.avans.vsoprj2.wordcrex.controllers.administrator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController extends Controller {
    @FXML
    public ComboBox userComboBox;

    @FXML
    public Label currentUser;

    @FXML
    public Button changeUserRoleButton;

    @FXML
    public TextInputControl searchInput;

    @FXML
    public CheckBox checkBoxPlayer;

    @FXML
    public CheckBox checkBoxModerator;

    @FXML
    public CheckBox checkBoxObserver;

    @FXML
    public CheckBox checkBoxAdministrator;

    @FXML
    private void handleBackButton() {
        this.navigateTo("/views/settings.fxml");
    }

    private ArrayList<Account.Role> accountRoles = new ArrayList<Account.Role>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        this.getAllUsers();
    }

    /**
     * Get all the roles of a selected or searched user
     *
     * @param username - selected or searched username
     */
    private void getUserRoles(String username) {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement userRoleStatement = connection.prepareStatement("SELECT `role` FROM `accountrole` WHERE `username` = ?");
            userRoleStatement.setString(1, username);
            ResultSet roles = userRoleStatement.executeQuery();
            this.accountRoles.clear();

            while (roles.next()) {
                this.accountRoles.add(Account.Role.valueOf(roles.getString("role").toUpperCase()));
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
            PreparedStatement usersStatement = connection.prepareStatement("SELECT * FROM `account`");
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
            this.checkBoxPlayer.setSelected(false);
            this.checkBoxModerator.setSelected(false);
            this.checkBoxObserver.setSelected(false);
            this.checkBoxAdministrator.setSelected(false);

            this.getUserRoles(account.getUsername());
            this.currentUser.setText(account.getUsername());
            this.currentUser.setVisible(true);
            this.changeUserRoleButton.setVisible(true);
            for (int i=0; i < this.accountRoles.size(); i++) {
                switch (this.accountRoles.get(i)) {
                    case PLAYER:
                        this.checkBoxPlayer.setSelected(true);
                        break;
                    case MODERATOR:
                        this.checkBoxModerator.setSelected(true);
                        break;
                    case OBSERVER:
                        this.checkBoxObserver.setSelected(true);
                        break;
                    case ADMINISTRATOR:
                        this.checkBoxAdministrator.setSelected(true);
                        break;
                }
            }
            this.checkBoxPlayer.setVisible(true);
            this.checkBoxModerator.setVisible(true);
            this.checkBoxObserver.setVisible(true);
            this.checkBoxAdministrator.setVisible(true);
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
            //TODO gebruiker bestaat niet error
            this.userComboBox.getSelectionModel().select(null);
            PreparedStatement userStatement = connection.prepareStatement("SELECT username FROM account WHERE username=?");
            userStatement.setString(1, this.searchInput.getText().trim());
            ResultSet user = userStatement.executeQuery();

            //TODO check of de gebruiker klopt
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
                    PreparedStatement oldRolesStatement = connection.prepareStatement("DELETE FROM `accountrole` WHERE `username` = ?");
                    oldRolesStatement.setString(1, this.currentUser.getText());
                    oldRolesStatement.executeUpdate();

                    StringBuilder insertQuery = new StringBuilder("INSERT INTO `accountrole`(`username`, `role`) VALUES ");
                    boolean firstInsert = true;
                    if(this.checkBoxPlayer.isSelected()) {
                        insertQuery.append(" ('").append(this.currentUser.getText()).append("', 'player')");
                        firstInsert = false;
                    }
                    if(this.checkBoxModerator.isSelected()) {
                        insertQuery.append(firstInsert ? "" : ",").append(" ('").append(this.currentUser.getText()).append("', 'moderator')");
                        firstInsert = false;
                    }
                    if(this.checkBoxObserver.isSelected()) {
                        insertQuery.append(firstInsert ? "" : ",").append(" ('").append(this.currentUser.getText()).append("', 'observer')");
                        firstInsert = false;
                    }
                    if(this.checkBoxAdministrator.isSelected()) {
                        insertQuery.append(firstInsert ? "" : ",").append(" ('").append(this.currentUser.getText()).append("', 'administrator')");
                        firstInsert = false;
                    }
                    insertQuery.append(";");
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery.toString());

                    if(!firstInsert) insertStatement.executeUpdate();

                    //reset user combobox to get the new role of the changed player
                    this.userComboBox.getItems().clear();
                    this.getAllUsers();

                    //reset form
                    this.currentUser.setVisible(false);
                    this.checkBoxPlayer.setVisible(false);
                    this.checkBoxModerator.setVisible(false);
                    this.checkBoxAdministrator.setVisible(false);
                    this.checkBoxObserver.setVisible(false);
                    this.changeUserRoleButton.setVisible(false);
                    this.searchInput.setText("");

                } catch (SQLException ex) {
                    if(WordCrex.DEBUG_MODE) {
                        System.err.println(ex.toString());
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
