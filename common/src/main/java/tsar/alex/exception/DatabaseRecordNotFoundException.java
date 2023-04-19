package tsar.alex.exception;

public class DatabaseRecordNotFoundException extends RuntimeException {
    public DatabaseRecordNotFoundException(String message) {
        super(message);
    }
}
