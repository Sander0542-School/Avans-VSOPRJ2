package nl.avans.vsoprj2.wordcrex;

import nl.avans.vsoprj2.wordcrex.exceptions.DbConnectionException;
import nl.avans.vsoprj2.wordcrex.models.Account;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Singleton {

    private static Singleton INSTANCE;

    private Account account;
    private Connection connection;

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Singleton();
        }

        return INSTANCE;
    }

    public Account getUser() {
        return this.account;
    }

    public void setUser(Account account) {
        this.account = account;
    }

    public Connection getConnection() {
        if (this.connection == null) {
            try {
                this.connection = DriverManager.getConnection("jdbc:mysql://tommyhosewol.com/avans_wordcrex", "wordcrex", "EdiILXhe1fK04mvA");
            } catch (SQLException e) {
                throw new DbConnectionException(e);
            }
        }

        return this.connection;
    }
}
