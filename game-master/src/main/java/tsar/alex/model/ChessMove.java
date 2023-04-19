package tsar.alex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChessMove {

    @Min(value = 0, message = "moveNumber is less than 0")
    @Max(value = 9000, message = "moveNumber is greater than 9000")
    private int moveNumber;

    @NotNull(message = "startPiece is null")
    private ChessPieceEnum startPiece;

    @NotNull(message = "startCoords field is null")
    private ChessCoords startCoords;

    private ChessPieceEnum endPiece;

    @NotNull(message = "endCoords field is null")
    private ChessCoords endCoords;

    @Min(value = -1, message = "castling is less than -1")
    @Max(value = 1, message = "castling is greater than 9000")
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

    public void setTimeLeftByUserColor(ChessColor userColor, long timeLeftMS) {
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
