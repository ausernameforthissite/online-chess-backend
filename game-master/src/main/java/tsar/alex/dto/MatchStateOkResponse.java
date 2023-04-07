package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tsar.alex.model.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchStateOkResponse implements MatchStateResponse {
    private boolean finished;
    private UsersInMatch usersInMatch;
    private ChessMatchResult matchResult;
    private List<ChessMove> matchRecord;
}