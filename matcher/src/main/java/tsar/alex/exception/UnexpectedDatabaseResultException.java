package tsar.alex.exception;

public class UnexpectedDatabaseResultException extends RuntimeException {
    public UnexpectedDatabaseResultException(String message) {
        super(message);
    }
}
