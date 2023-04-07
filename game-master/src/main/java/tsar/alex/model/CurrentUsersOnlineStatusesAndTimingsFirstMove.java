package tsar.alex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CurrentUsersOnlineStatusesAndTimingsFirstMove extends CurrentUsersOnlineStatusesAndTimings {
    private long userFirstMoveTimeLeftMS;

    public CurrentUsersOnlineStatusesAndTimingsFirstMove(boolean whiteUserOnline, long whiteTimeLeftMS,
                                                         long whiteReconnectTimeLeftMS, boolean blackUserOnline,
                                                         long blackTimeLeftMS, long blackReconnectTimeLeftMS,
                                                         long userFirstMoveTimeLeftMS) {
        super(whiteUserOnline, whiteTimeLeftMS, whiteReconnectTimeLeftMS, blackUserOnline,
                blackTimeLeftMS, blackReconnectTimeLeftMS);
        this.userFirstMoveTimeLeftMS = userFirstMoveTimeLeftMS;

    }

}