package tsar.alex.exception;

public class RestApiResponseException extends RuntimeException {
    public RestApiResponseException(String message) {
        super(message);
    }
}