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
public class Match implements Comparable<Match> {

    @Id
    private long id;
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

    @Override
    public int compareTo(Match o) {
        return Long.compare(id, o.id);
    }
}
