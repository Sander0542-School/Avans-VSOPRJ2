package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;
import java.util.Date;

public class ChatMessage extends Model {
    @Column("username")
    private String username; // TODO Change to user model instead of just username
    @Column("moment")
    private Date date;
    @Column("message")
    private String message;

    public ChatMessage(ResultSet resultSet) {
        super(resultSet);
    }

    public String getMessage() {
        return this.message;
    }

    public Date getDate() {
        return this.date;
    }

    public String getUsername() {
        return this.username;
    }
}
