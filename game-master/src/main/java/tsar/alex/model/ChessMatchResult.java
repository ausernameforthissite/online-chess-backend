package tsar.alex.model;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChessMatchResult {
    private boolean technicalFinish;
    private boolean draw;
    private ChessColor winnerColor;
    private String message;

    private long whiteTimeLeftMS;
    private long blackTimeLeftMS;

    public ChessMatchResult(ChessColor winnerColor, String message) {
        this.winnerColor = winnerColor;
        this.message = message;
    }

    public ChessMatchResult(boolean technicalFinish, String message) {
        this.technicalFinish = technicalFinish;
        this.message = message;
    }

    public void setTimeLeftByUserColor(ChessColor  userColor, long timeLeftMS) {
        switch(userColor) {
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
