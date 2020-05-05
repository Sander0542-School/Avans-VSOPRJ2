package nl.avans.vsoprj2.wordcrex;

import nl.avans.vsoprj2.wordcrex.exceptions.DbConnectionException;
import nl.avans.vsoprj2.wordcrex.models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Singleton {

    private static Singleton INSTANCE;

    private User user;
    private Connection connection;

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Singleton();
        }

        return INSTANCE;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://tommyhosewol.com/avans_wordcrex", "wordcrex", "EdiILXhe1fK04mvA");
            } catch (SQLException e) {
                throw new DbConnectionException(e);
            }
        }

        return connection;
    }
}
