package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tsar.alex.model.ChessMove;
import tsar.alex.model.Match;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchStateOkResponse implements MatchStateResponse {
    private Match match;
    private List<ChessMove> matchRecord;
}
