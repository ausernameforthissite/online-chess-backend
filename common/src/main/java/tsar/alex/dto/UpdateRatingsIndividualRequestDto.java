package tsar.alex.dto;

import static tsar.alex.utils.Constants.GAME_ID_REGEX;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRatingsIndividualRequestDto {
    @NotBlank(message = "gameId is blank")
    @Pattern(regexp = GAME_ID_REGEX, message = "Incorrect gameId")
    private String gameId;
    private boolean technicalFinish;
    private boolean draw;
    private ChessColor winnerColor;
}
