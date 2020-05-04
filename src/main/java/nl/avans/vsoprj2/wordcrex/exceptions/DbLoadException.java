package nl.avans.vsoprj2.wordcrex.exceptions;

public class DbLoadException extends RuntimeException {
    public DbLoadException(Throwable cause) {
        super("Could load data from the database", cause);
    }
}
