package tsar.alex.dto.response;

import static tsar.alex.utils.Constants.GAME_ID_REGEX;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartGameOkResponse implements StartGameResponse, RestApiOkResponse {
    @NotBlank(message = "gameId must not be blank")
    @Pattern(regexp = GAME_ID_REGEX, message = "Incorrect gameId")
    private String gameId;

    @NotNull(message = "startedAt is null")
    private Instant startedAt;

    private boolean sameUsersOrder;
}
