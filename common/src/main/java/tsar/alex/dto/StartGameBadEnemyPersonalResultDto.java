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
public class StartGameBadEnemyPersonalResultDto implements StartGamePersonalResultDto {
    @NotBlank(message = "username is blank")
    private String username;
}