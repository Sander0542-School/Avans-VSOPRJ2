package nl.avans.vsoprj2.wordcrex.exceptions;

public class DbConnectionException extends RuntimeException {
    public DbConnectionException(Throwable cause) {
        super("Could not connect to the database", cause);
    }
}
