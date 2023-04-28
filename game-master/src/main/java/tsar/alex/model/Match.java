package tsar.alex.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "match")
public class Match {

    @Id
    private String id;
    private ChessGameTypeWithTimings gameType;
    private Instant startedAt;
    private long lastMoveTimeMS;
    private boolean finished;
    private UsersInMatch usersInMatch;
    private ChessCoords enPassantPawnCoords;
    private ChessPiece[][] boardState;
    private ChessMatchResult result;
    private List<ChessMove> chessMovesRecord;

    private ChessPositionsRecord chessPositionsRecord;


    public int getCurrentMoveNumber() {
        return chessMovesRecord == null ? 0 : chessMovesRecord.size();
    }
}
