package tsar.alex.dto.response;

import lombok.ToString;

@ToString(callSuper = true)
public class RegisterBadResponse extends GeneralBadResponse implements RegisterResponse {
    public RegisterBadResponse(String message) {
        super(message);
    }
}