package tsar.alex.model;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersInGameWithOnlineStatusesAndTimings extends UsersInGame {
    private boolean whiteUserOnline;
    private boolean blackUserOnline;
    private long lastMoveWhiteTimeLeftMS;
    private long lastMoveBlackTimeLeftMS;

    public UsersInGameWithOnlineStatusesAndTimings(UsersInGame usersInGame) {
        super(usersInGame.getWhiteUsername(), usersInGame.getBlackUsername());
    }

    public void setOnlineStatusByUserColor(ChessColor userColor, boolean onlineStatus) {
        switch (userColor) {
            case WHITE:
                this.whiteUserOnline = onlineStatus;
                break;
            case BLACK:
                this.blackUserOnline = onlineStatus;
                break;
            default:
                throw new RuntimeException("Incorrect user color: " + userColor);
        }
    }

    public void setTimeLefts(@NotNull ChessMove chessMove) {
        this.lastMoveWhiteTimeLeftMS = chessMove.getWhiteTimeLeftMS();
        this.lastMoveBlackTimeLeftMS = chessMove.getBlackTimeLeftMS();
    }

    public long getTimeLeftByUserColor(ChessColor userColor) {
        switch (userColor) {
            case WHITE:
                return this.lastMoveWhiteTimeLeftMS;
            case BLACK:
                return this.lastMoveBlackTimeLeftMS;
            default:
                throw new RuntimeException("Incorrect chess color type");
        }
    }

}
