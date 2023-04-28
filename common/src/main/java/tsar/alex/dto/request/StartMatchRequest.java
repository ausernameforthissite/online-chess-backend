package tsar.alex.dto.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.model.ChessGameTypeWithTimings;
import tsar.alex.model.Pair;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StartMatchRequest {

    @NotNull
    private ChessGameTypeWithTimings chessGameTypeWithTimings;

    @NotNull
    private Pair<String> pairOfUsernames;
}