package tsar.alex.utils.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tsar.alex.model.*;
import tsar.alex.service.MatchWebsocketService;
import tsar.alex.utils.ChessGameConstants;
import tsar.alex.utils.ChessGameUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@Component
@RequiredArgsConstructor
public class ChessMatchWebsocketRoomsHolder {
    private final Map<Long, ChessMatchWebsocketRoom> matchWebsocketRooms = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;

    private final MatchWebsocketService matchWebsocketService;

    @PostConstruct
    private void postConstruct() {
        List<Match> notFinishedMatches = matchWebsocketService.getNotFinishedMatches();
        for (Match match : notFinishedMatches) {
            List<ChessMove> chessMoves = match.getChessMovesRecord();

            long delay;
            int currentMoveNumber = match.getCurrentMoveNumber();
            ChessColor currentTurnUserColor = ChessGameUtils.getUserColorByMoveNumber(currentMoveNumber);

            if (currentMoveNumber < 2) {
                delay = ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS;
            } else {
                ChessMove lastMove = chessMoves.get(currentMoveNumber - 1);
                delay = lastMove.getTimeLeftByUserColor(currentTurnUserColor)
                        - (System.currentTimeMillis() - match.getLastMoveTimeMS());
            }

            ChessMatchWebsocketRoom websocketRoom = addMatchWebsocketRoom(match.getId(), match.getUsersInMatch());

            websocketRoom.reentrantLock.lock();

            try {
                websocketRoom.checkFinished();
                websocketRoom.setLastMoveNumber(currentMoveNumber - 1);
                websocketRoom.setTimeoutFinisher(currentTurnUserColor, TimeoutTypeEnum.TIME_IS_UP, delay);

                if (currentMoveNumber >= 2) {
                    websocketRoom.setTimeoutFinisher(ChessColor.WHITE, TimeoutTypeEnum.DISCONNECTED,
                            ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
                    websocketRoom.setTimeoutFinisher(ChessColor.BLACK, TimeoutTypeEnum.DISCONNECTED,
                            ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
                }
            } finally {
                websocketRoom.reentrantLock.unlock();
            }
        }
    }

    public ChessMatchWebsocketRoom addMatchWebsocketRoom(long matchId, UsersInMatch usersInMatch) {
        if (matchWebsocketRooms.containsKey(matchId)) {
            throw new RuntimeException("Room with match id = " + matchId + " already exists!");
        } else {
            ChessMatchWebsocketRoom websocketRoom = new ChessMatchWebsocketRoom(this,
                                                    matchId, new UsersInMatchWithOnlineStatus(usersInMatch), objectMapper,
                                                    messagingTemplate, scheduledExecutorService, matchWebsocketService);

            matchWebsocketRooms.put(matchId, websocketRoom);
            return websocketRoom;
        }
    }

    public ChessMatchWebsocketRoom getMatchWebsocketRoom(long matchId) {
        return matchWebsocketRooms.get(matchId);
    }

    public ChessMatchWebsocketRoom removeMatchWebsocketRoom(long matchId) {
        return matchWebsocketRooms.remove(matchId);
    }

}
