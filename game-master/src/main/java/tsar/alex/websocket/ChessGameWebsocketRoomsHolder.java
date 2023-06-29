package tsar.alex.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tsar.alex.model.*;
import tsar.alex.service.GameWebsocketService;
import tsar.alex.utils.ChessGameConstants;
import tsar.alex.utils.ChessGameUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import tsar.alex.websocket.ChessGameWebsocketRoom;

@Component
@RequiredArgsConstructor
public class ChessGameWebsocketRoomsHolder {
    private final Map<String, ChessGameWebsocketRoom> gameWebsocketRooms = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;

    private final GameWebsocketService gameWebsocketService;

    @PostConstruct
    private void postConstruct() {
        List<Game> notFinishedGames = gameWebsocketService.getNotFinishedGames();
        for (Game game : notFinishedGames) {
            List<ChessMove> chessMoves = game.getChessMovesRecord();

            long delay;
            int currentMoveNumber = game.getCurrentMoveNumber();
            int lastMoveNumber = currentMoveNumber - 1;
            ChessColor currentTurnUserColor = ChessGameUtils.getUserColorByMoveNumber(currentMoveNumber);

            UsersInGameWithOnlineStatusesAndTimings usersInGame = new UsersInGameWithOnlineStatusesAndTimings(game.getUsersInGame());

            if (currentMoveNumber < 2) {
                delay = ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS;
            } else {
                ChessMove lastMove = chessMoves.get(lastMoveNumber);
                usersInGame.setTimeLefts(lastMove);
                delay = lastMove.getTimeLeftByUserColor(currentTurnUserColor)
                        - (System.currentTimeMillis() - game.getLastMoveTimeMS());
            }

            ChessGameWebsocketRoom websocketRoom = addGameWebsocketRoom(game.getId(), game.getGameType(), usersInGame);
            websocketRoom.reentrantLock.lock();

            try {
                websocketRoom.setLastMoveNumber(lastMoveNumber);
                websocketRoom.setTimeoutDisconnectTask(currentTurnUserColor, TimeoutTypeEnum.TIME_IS_UP, delay);

                if (currentMoveNumber >= 2) {
                    websocketRoom.setTimeoutDisconnectTask(ChessColor.WHITE, TimeoutTypeEnum.DISCONNECTED,
                            ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
                    websocketRoom.setTimeoutDisconnectTask(ChessColor.BLACK, TimeoutTypeEnum.DISCONNECTED,
                            ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
                }
            } finally {
                websocketRoom.reentrantLock.unlock();
            }
        }
    }

    public ChessGameWebsocketRoom addGameWebsocketRoom(String gameId, ChessGameTypeWithTimings gameType, UsersInGameWithOnlineStatusesAndTimings usersInGameWithOnlineStatusesAndTimings) {
        if (gameWebsocketRooms.containsKey(gameId)) {
            throw new RuntimeException("Room with Game id = " + gameId + " already exists!");
        } else {
            ChessGameWebsocketRoom websocketRoom = new ChessGameWebsocketRoom(gameId, gameType,
                    usersInGameWithOnlineStatusesAndTimings,this, objectMapper,
                    messagingTemplate, scheduledExecutorService, gameWebsocketService);

            gameWebsocketRooms.put(gameId, websocketRoom);
            return websocketRoom;
        }
    }

    public ChessGameWebsocketRoom addGameWebsocketRoom(String gameId, ChessGameTypeWithTimings gameType, UsersInGame usersInGame) {
        return addGameWebsocketRoom(gameId, gameType, new UsersInGameWithOnlineStatusesAndTimings(usersInGame));
    }

    public ChessGameWebsocketRoom getGameWebsocketRoom(String gameId) {
        return gameWebsocketRooms.get(gameId);
    }

    public ChessGameWebsocketRoom removeGameWebsocketRoom(String gameId) {
        return gameWebsocketRooms.remove(gameId);
    }

}
