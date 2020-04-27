package nl.avans.vsoprj2.wordcrex;

import nl.avans.vsoprj2.wordcrex.exceptions.DbConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Singleton {

    private static Singleton INSTANCE;

    private Object user;
    private Connection connection;

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Singleton();
        }

        return INSTANCE;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://tommyhosewol.com/avans_wordcrex", "wordcrex", "EdiILXhe1fK04mvA");
            } catch (SQLException e) {
            }
        }

        return connection;
    }
}
