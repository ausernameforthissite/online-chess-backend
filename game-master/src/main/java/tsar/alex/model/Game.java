package tsar.alex.model;

import java.util.ArrayList;
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
@Document(collection = "game")
@ToString
public class Game {

    @Id
    private String id;
    private ChessGameTypeWithTimings gameType;
    private Instant startedAt;
    private long lastMoveTimeMS;
    private boolean finished;
    private UsersInGame usersInGame;
    private ChessCoords enPassantPawnCoords;
    private ChessPiece[][] boardState;
    private ChessGameResult result;
    private List<ChessMove> chessMovesRecord = new ArrayList<>();
    private ChessPositionsRecord chessPositionsRecord;

    public int getCurrentMoveNumber() {
        return chessMovesRecord == null ? 0 : chessMovesRecord.size();
    }
}