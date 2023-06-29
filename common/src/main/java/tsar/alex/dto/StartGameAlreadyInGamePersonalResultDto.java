package tsar.alex.dto;

import static tsar.alex.utils.Constants.GAME_ID_REGEX;

import java.time.Instant;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.model.ChessGameTypeWithTimings;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartGameAlreadyInGamePersonalResultDto implements StartGamePersonalResultDto {

    @NotBlank(message = "requestingUsername is blank")
    private String requestingUsername;

    @NotBlank(message = "gameId is blank")
    @Pattern(regexp = GAME_ID_REGEX, message = "Incorrect gameId")
    private String gameId;

    @NotNull(message = "gameType is null")
    private ChessGameTypeWithTimings gameType;

    @NotNull(message = "startedAt is null")
    private Instant startedAt;

    @NotBlank(message = "whiteUsername is blank")
    private String whiteUsername;

    @NotBlank(message = "blackUsername is blank")
    private String blackUsername;
}