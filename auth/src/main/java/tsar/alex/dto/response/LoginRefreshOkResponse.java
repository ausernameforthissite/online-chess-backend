package tsar.alex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginRefreshOkResponse implements LoginRefreshResponse, RestApiOkResponse {
    private String accessToken;
}