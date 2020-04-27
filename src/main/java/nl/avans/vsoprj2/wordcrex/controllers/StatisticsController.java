package nl.avans.vsoprj2.wordcrex.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nl.avans.vsoprj2.wordcrex.models.User;

import java.sql.*;

public class StatisticsController extends Controller {
    @FXML
    Label name;

    @FXML
    Label gamesWon;

    @FXML
    Label gamesLost;

    private User user;
    Connection connection = null;

    @FXML
    private void initialize() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://tommyhosewol.com/avans_wordcrex?user=wordcrex&password=EdiILXhe1fK04mvA");
        } catch (SQLException exception) {
            System.err.println("SQLException: " + exception.getMessage());
            System.err.println("SQLState: " + exception.getSQLState());
            System.err.println("VendorError: " + exception.getErrorCode());
        }


        // TEMP 4 USER
        user = new User();
        user.setUsername("Lidewij");
        user.setPassword("rrr");

        getTableData();
        setTableData();
    }

    private void setTableData() {
        name.setText(user.getUsername());

    }

    private void getTableData() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String query = null;

        try {
            query = "SELECT COUNT(`game_id`) as `games_won` FROM `game` WHERE `username_winner` = ?;";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            rs = stmt.executeQuery();

            while (rs.next()) {
                gamesWon.setText(rs.getString(1));
            }

            query = "SELECT COUNT(`game_id`) as `games_lost` FROM `game` WHERE `username_winner` != ? AND `username_player1` = ? OR `username_player2` = ?;";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getUsername());
            rs = stmt.executeQuery();

            while (rs.next()) {
                gamesLost.setText(rs.getString(1));
            }

            // Now do something with the ResultSet ....
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                stmt = null;
            }
        }

    }


}
