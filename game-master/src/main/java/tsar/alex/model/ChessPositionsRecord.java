package tsar.alex.model;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsar.alex.model.chessPieces.Bishop;
import tsar.alex.model.chessPieces.Knight;
import tsar.alex.model.chessPieces.Pawn;
import tsar.alex.model.chessPieces.Queen;
import tsar.alex.model.chessPieces.Rook;
import tsar.alex.utils.ChessGameUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ChessPositionsRecord {

    private int irreversibleChessPositionMoveNumber = -1;
    private int numberOfPiecesLeftOnBoard = 32;
    private boolean insufficientMaterialCheckIsRequired;
    private Map<String, Integer> chessPositions = new HashMap<>();

    public void clearChessPositionsRecord(boolean pieceCaptured, int moveNumber) {
        irreversibleChessPositionMoveNumber = moveNumber;
        chessPositions = new HashMap<>();

        if (pieceCaptured) {
            numberOfPiecesLeftOnBoard--;
            insufficientMaterialCheckIsRequired = true;
        }
    }

    public boolean isMaterialInsufficient(ChessPiece[][] boardState) {
        if (insufficientMaterialCheckIsRequired) {
            insufficientMaterialCheckIsRequired = false;

            if (numberOfPiecesLeftOnBoard < 13) {
                boolean knightIsPresent = false;
                ChessColor firstBishopSquareColor = null;

                for (int i = 0; i < BOARD_LENGTH; i++) {
                    for (int j = 0; j < BOARD_LENGTH; j++) {
                        if (boardState[i][j] != null) {
                            if (boardState[i][j] instanceof Pawn || boardState[i][j] instanceof Queen
                                    || boardState[i][j] instanceof Rook) {
                                return false;
                            }

                            if (boardState[i][j] instanceof Knight) {
                                if (knightIsPresent || firstBishopSquareColor != null) {
                                    return false;
                                } else {
                                    knightIsPresent = true;
                                }

                            } else if (boardState[i][j] instanceof Bishop) {
                                if (knightIsPresent) {
                                    return false;
                                }

                                if (firstBishopSquareColor == null) {
                                    firstBishopSquareColor = ChessGameUtils.getChessSquareColorByCoords(i, j);
                                } else if (firstBishopSquareColor != ChessGameUtils.getChessSquareColorByCoords(i, j)) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                return true;
            }

        }

        return false;
    }


    public ChessPositionStatusEnum handleNewChessPosition(int moveNumber, ChessPiece[][] boardState) {

        if (isMaterialInsufficient(boardState)) {
            return ChessPositionStatusEnum.INSUFFICIENT_MATERIAL;
        }

        if ((moveNumber - irreversibleChessPositionMoveNumber) >= 100) {
            return ChessPositionStatusEnum.FIFTY_MOVE_NOT_CHANGE;
        }

        String simplifiedBoardStateString = ChessGameUtils.boardStateToSimplifiedBoardStateString(boardState);
        Integer positionsRepetitions = chessPositions.get(simplifiedBoardStateString);

        if (positionsRepetitions == null) {
            chessPositions.put(simplifiedBoardStateString, 1);
        } else if (positionsRepetitions == 2) {
            return ChessPositionStatusEnum.THREEFOLD_REPETITION;
        } else {
            chessPositions.put(simplifiedBoardStateString, positionsRepetitions + 1);
        }

        return ChessPositionStatusEnum.OK;
    }
}
