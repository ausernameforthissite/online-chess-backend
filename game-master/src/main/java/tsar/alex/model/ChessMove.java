package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChessMove {
    @NotNull
    private ChessPieceEnum startPiece;

    @NotNull
    private ChessCoords startCoords;

    private ChessPieceEnum endPiece;

    @NotNull
    private ChessCoords endCoords;
    private int castling;
    private ChessPieceEnum pawnPromotionPiece;
    private ChessMoveResult chessMoveResult;

    private boolean startPieceFirstMove;
    private boolean endPieceFirstMove;
    private ChessCoords previousEnPassantCoords;
}
