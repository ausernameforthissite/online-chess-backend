package tsar.alex.exception;

public class MatchDatabaseNotFoundException extends RuntimeException {
    public MatchDatabaseNotFoundException(String message) {
        super(message);
    }
}

