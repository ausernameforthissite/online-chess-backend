package tsar.alex.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersInMatchWithOnlineStatus extends UsersInMatch {
    private boolean whiteUserOnline;
    private boolean blackUserOnline;

    public UsersInMatchWithOnlineStatus(UsersInMatch usersInMatch) {
        super(usersInMatch.getWhiteUsername(), usersInMatch.getBlackUsername());
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

}
