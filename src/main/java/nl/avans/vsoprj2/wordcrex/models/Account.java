package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class Account extends Model {

    @Column("username")
    private String username;
    @Column("password")
    private String password;

    public String getTable() {
        return "account";
    }

    public Account(ResultSet resultSet) {
        super(resultSet);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
