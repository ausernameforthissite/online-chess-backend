package tsar.alex.dto.response;

import lombok.ToString;

@ToString(callSuper = true)
public class LoginRefreshBadResponse extends GeneralBadResponse implements LoginRefreshResponse {
    public LoginRefreshBadResponse(String message) {
        super(message);
    }
}