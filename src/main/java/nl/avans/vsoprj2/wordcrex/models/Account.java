package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class Account extends Model {
    @Column("username")
    private String username;
    @Column("role")
    private String role;

    public Account(ResultSet resultSet) {
        super(resultSet);
    }

    public String getTable() {
        return "account";
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
