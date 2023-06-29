package tsar.alex.exception;

public class UnexpectedDatabaseQueryResultException extends RuntimeException {
    public UnexpectedDatabaseQueryResultException(String message) {
        super(message);
    }
}