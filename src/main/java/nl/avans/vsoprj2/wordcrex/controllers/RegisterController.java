package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import nl.avans.vsoprj2.wordcrex.exceptions.DbConnectionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController extends Controller  {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private PasswordField repeatpassword;


    @FXML protected void handleRegisterAction(ActionEvent event) {
        //register
        //validation

        navigateTo("/views/login.fxml");
    }

}
