package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.dto.response.RestApiOkResponse;
import tsar.alex.model.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameStateOkResponse implements GameStateResponse, RestApiOkResponse {
    private boolean finished;
    private UsersInGame usersInGame;
    private ChessGameResult gameResult;
    private List<ChessMove> gameRecord;
}