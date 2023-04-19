package tsar.alex.dto.response;



public class LogoutBadResponse extends GeneralBadResponse implements LogoutResponse {
    public LogoutBadResponse(String message) {
        super(message);
    }
}