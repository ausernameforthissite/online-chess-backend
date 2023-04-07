package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.utils.ChessGameUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChessPositionsRecord {
    private int irreversibleChessPositionMoveNumber = -1;
    private Map<String, Integer> chessPositions = new HashMap<>();

    public ChessPositionCheckResultEnum handleNewChessPosition(int moveNumber, ChessPiece[][] boardState) {
        if ((moveNumber - irreversibleChessPositionMoveNumber) >= 100) {
            return ChessPositionCheckResultEnum.FIFTY_MOVE_NOT_CHANGE;
        }

        String simplifiedBoardStateString = ChessGameUtils.boardStateToSimplifiedBoardStateString(boardState);

        Integer positionsRepetitions = chessPositions.get(simplifiedBoardStateString);

        if (positionsRepetitions == null) {
            chessPositions.put(simplifiedBoardStateString, 1);
        } else if (positionsRepetitions == 2) {
            return ChessPositionCheckResultEnum.THREEFOLD_REPETITION;
        } else {
            chessPositions.put(simplifiedBoardStateString, positionsRepetitions + 1);
        }

        return ChessPositionCheckResultEnum.OK;
    }
}
