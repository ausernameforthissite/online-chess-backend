package tsar.alex.dto.request;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.model.ChessGameTypeWithTimings;
import tsar.alex.validation.EvenNumberOfElements;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartGameRequest {
    @NotNull
    private ChessGameTypeWithTimings gameType;

    @NotEmpty(message = "Usernames list is empty")
    @EvenNumberOfElements(message = "Usernames list has an odd number of elements")
    private List<@NotBlank(message = "Username is blank") String> usernames;
}
