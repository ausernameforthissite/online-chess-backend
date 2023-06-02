package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.CurrentUsersOnlineStatusesAndTimings;

@Getter
@Setter
public class ChessGameInfoResponse extends ChessGameWebsocketResponse {
    private int lastMoveNumber;
    private CurrentUsersOnlineStatusesAndTimings currentUsersOnlineStatusesAndTimings;
    private long initialTimeLeftMS;
    private long initialFirstMoveTimeLeftMS;
    private long initialReconnectTimeLeftMS;


    public ChessGameInfoResponse(int lastMoveNumber,
                                  CurrentUsersOnlineStatusesAndTimings currentUsersOnlineStatusesAndTimings,
                                  long initialTimeLeftMS,
                                  long initialFirstMoveTimeLeftMS,
                                  long initialReconnectTimeLeftMS) {
        super(ChessGameWebsocketResponseEnum.INFO);
        this.lastMoveNumber = lastMoveNumber;
        this.currentUsersOnlineStatusesAndTimings = currentUsersOnlineStatusesAndTimings;
        this.initialTimeLeftMS = initialTimeLeftMS;
        this.initialFirstMoveTimeLeftMS = initialFirstMoveTimeLeftMS;
        this.initialReconnectTimeLeftMS = initialReconnectTimeLeftMS;
    }
}