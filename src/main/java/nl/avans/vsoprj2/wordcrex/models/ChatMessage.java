package nl.avans.vsoprj2.wordcrex.models;

import java.util.Date;

public class ChatMessage {
    private String username; // TODO Change to user model instead of just username
    private Date date;
    private String message;

    public ChatMessage(String username, Date date, String message) {
        this.username = username;
        this.date = date;
        this.message = message;
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
