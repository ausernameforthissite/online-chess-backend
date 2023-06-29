package tsar.alex.dto.response;

import java.time.Instant;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.dto.StartGamePersonalResultDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartGameOkResponse implements StartGameResponse, RestApiOkResponse {

    @NotNull(message = "startedAt is null")
    private Instant startedAt;

    @NotEmpty(message = "personalResults list is empty")
    private List<@NotNull(message = "startGamePersonalResultDto is null") StartGamePersonalResultDto> personalResults;
}