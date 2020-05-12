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

    @Override
    public String getTable() {
        return null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return this.role;
    }
}
