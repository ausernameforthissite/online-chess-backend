package tsar.alex.dto;


import static tsar.alex.utils.Constants.GAME_ID_REGEX;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartGameOkPersonalResultDto implements StartGamePersonalResultDto {

    @NotBlank(message = "gameId is blank")
    @Pattern(regexp = GAME_ID_REGEX, message = "Incorrect gameId")
    private String gameId;

    @NotBlank(message = "whiteUsername is blank")
    private String whiteUsername;

    @NotBlank(message = "blackUsername is blank")
    private String blackUsername;
}