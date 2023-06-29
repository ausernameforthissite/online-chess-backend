package tsar.alex.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartGameMultipleActiveGamesPersonalResultDto implements StartGamePersonalResultDto {

    @NotBlank(message = "username is blank")
    private String username;

    @NotBlank(message = "message is blank")
    private String message;
}