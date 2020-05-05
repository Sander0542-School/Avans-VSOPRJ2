package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class User extends Model {
    @Column("username")
    private String username;
    @Column("role")
    private String role;

    public User(ResultSet resultSet) {
        super(resultSet);
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
