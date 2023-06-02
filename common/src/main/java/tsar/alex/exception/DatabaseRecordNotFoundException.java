package tsar.alex.exception;

public class DatabaseRecordNotFoundException extends UnexpectedDatabaseQueryResultException {
    public DatabaseRecordNotFoundException(String message) {
        super(message);
    }
}