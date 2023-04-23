package tsar.alex.dto.response;

import static tsar.alex.utils.Constants.MATCH_ID_REGEX;

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
public class StartMatchOkResponse implements StartMatchResponse, RestApiOkResponse {
    @NotBlank(message = "matchId must not be blank")
    @Pattern(regexp = MATCH_ID_REGEX, message = "Incorrect matchId")
    private String matchId;

    @NotNull(message = "startedAt is null")
    private Instant startedAt;

    private boolean sameUsersOrder;
}
