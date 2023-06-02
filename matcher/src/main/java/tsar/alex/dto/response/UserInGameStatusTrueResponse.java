package tsar.alex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInGameStatusTrueResponse implements UserInGameStatusResponse {
    private String gameId;
}
