package tsar.alex.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tsar.alex.api.client.GameMasterRestClient;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.mapper.GameMasterMapper;
import tsar.alex.model.*;
import tsar.alex.repository.GameRepository;
import tsar.alex.utils.ChessGameUtils;


import java.util.List;
import tsar.alex.utils.GameMasterUtils;


@Service
@AllArgsConstructor
@Slf4j
public class GameWebsocketService {

    private final GameMasterRestClient gameMasterRestClient;
    private final GameMasterMapper mapper;

    private final GameRepository gameRepository;
    private final UpdateRatingsService updateRatingsService;


    public List<Game> getNotFinishedGames() {
        return gameRepository.getGamesByFinishedFalse();
    }

    public ChessGameResult finishGameByTimeout(String gameId, ChessColor timeoutUserColor,
                                                 TimeoutTypeEnum timeoutType) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new DatabaseRecordNotFoundException(
                                                "No game with id = " + gameId + " was found in game DB."));
        game.setFinished(true);
        String message;
        ChessGameResult gameResult;
        int currentMoveNumber = game.getCurrentMoveNumber();

        if (currentMoveNumber < 2) {
            message = "The " + timeoutUserColor.name().toLowerCase() + " player didn't make his first move.";
            gameResult = new ChessGameResult(true, message);
        } else {
            switch (timeoutType) {
                case TIME_IS_UP:
                    message = "The " + timeoutUserColor.name().toLowerCase() + " player's time is up.";
                    gameResult = new ChessGameResult(ChessColor.getInvertedColor(timeoutUserColor), message);
                    break;
                case DISCONNECTED:
                    message = (timeoutUserColor == ChessColor.WHITE ? "White" : "Black") + " player has left the game.";
                    gameResult = new ChessGameResult(ChessColor.getInvertedColor(timeoutUserColor), message);
                    break;
                default:
                    throw new RuntimeException("Incorrect timeout type: " + timeoutType);
            }
        }

        ChessGameUtils.setTimeLeftsToGameResult(gameResult, game, currentMoveNumber);

        game.setResult(gameResult);
        gameRepository.save(game);

        updateRatingsService.updateRatingsAfterGameFinished(List.of(game));

        return gameResult;
    }


    public ChessGameResult finishGame(String gameId, ChessGameResult gameResult) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new DatabaseRecordNotFoundException(
                "No game with id = " + gameId + " was found in game DB."));
        game.setFinished(true);

        int currentMoveNumber = game.getCurrentMoveNumber();
        ChessGameUtils.setTimeLeftsToGameResult(gameResult, game, currentMoveNumber);
        game.setResult(gameResult);
        gameRepository.save(game);

        updateRatingsService.updateRatingsAfterGameFinished(List.of(game));
        return gameResult;
    }
}
