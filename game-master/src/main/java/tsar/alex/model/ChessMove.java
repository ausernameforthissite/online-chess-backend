package tsar.alex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChessMove {

    private int moveNumber;

    @NotNull
    private ChessPieceEnum startPiece;

    @NotNull
    private ChessCoords startCoords;

    private ChessPieceEnum endPiece;

    @NotNull
    private ChessCoords endCoords;
    private int castling;
    private ChessPieceEnum pawnPromotionPiece;
    private ChessMoveResult result;

    private boolean startPieceFirstMove;
    private boolean endPieceFirstMove;
    private ChessCoords previousEnPassantCoords;

    private long whiteTimeLeftMS;
    private long blackTimeLeftMS;


    public long getTimeLeftByUserColor(ChessColor userColor) {
        switch (userColor) {
            case WHITE:
                return this.whiteTimeLeftMS;
            case BLACK:
                return this.blackTimeLeftMS;
            default:
                throw new RuntimeException("Incorrect ChessColor: " + userColor);
        }
    }

    public void setTimeLeftByUserColor(ChessColor  userColor, long timeLeftMS) {
        switch (userColor) {
            case WHITE:
                this.whiteTimeLeftMS = timeLeftMS;
                break;
            case BLACK:
                this.blackTimeLeftMS = timeLeftMS;
                break;
            default:
                throw new RuntimeException("Incorrect ChessColor: " + userColor);
        }
    }

}
