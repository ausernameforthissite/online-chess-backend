package tsar.alex.dto.response;


import lombok.ToString;

@ToString(callSuper = true)
public class LogoutBadResponse extends GeneralBadResponse implements LogoutResponse {
    public LogoutBadResponse(String message) {
        super(message);
    }
}