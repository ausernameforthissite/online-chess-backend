package tsar.alex.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tsar.alex.exception.MatchDatabaseNotFoundException;
import tsar.alex.mapper.GameMasterMapper;
import tsar.alex.model.*;
import tsar.alex.repository.MatchRepository;
import tsar.alex.utils.ChessGameUtils;
import tsar.alex.utils.Utils;


import java.util.List;


@Service
@AllArgsConstructor
public class MatchWebsocketService {

    private final GameMasterMapper mapper;

    private final MatchRepository matchRepository;


    public List<Match> getNotFinishedMatches() {
        return matchRepository.getMatchesByFinishedFalse();
    }

    public ChessMatchResult finishMatchByTimeout(long matchId, ChessColor timeoutUserColor,
                                                 TimeoutTypeEnum timeoutType) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new MatchDatabaseNotFoundException(
                                                "No match with id = " + matchId + " was found in match DB."));
        match.setFinished(true);
        String message;
        ChessMatchResult matchResult;
        int currentMoveNumber = match.getCurrentMoveNumber();

        if (currentMoveNumber < 2) {
            message = "The " + timeoutUserColor.name().toLowerCase() + " player didn't make his first move.";
            matchResult = new ChessMatchResult(true, message);
        } else {
            switch (timeoutType) {
                case TIME_IS_UP:
                    message = "The " + timeoutUserColor.name().toLowerCase() + " player's time is up.";
                    matchResult = new ChessMatchResult(ChessColor.getInvertedColor(timeoutUserColor), message);
                    break;
                case DISCONNECTED:
                    message = (timeoutUserColor == ChessColor.WHITE ? "White" : "Black") + " player has left the game.";
                    matchResult = new ChessMatchResult(ChessColor.getInvertedColor(timeoutUserColor), message);
                    break;
                default:
                    throw new RuntimeException("Incorrect timeout type: " + timeoutType);
            }
        }

        ChessGameUtils.setTimeLeftsToMatchResult(matchResult, match, currentMoveNumber);

        match.setResult(matchResult);
        matchRepository.save(match);

        Utils.sendUpdateUsersRatingsRequest(mapper.mapToUpdateUsersRatingsRequest(match));

        return matchResult;
    }


    public ChessMatchResult finishMatch(long matchId, ChessMatchResult matchResult) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new MatchDatabaseNotFoundException(
                "No match with id = " + matchId + " was found in match DB."));
        match.setFinished(true);

        int currentMoveNumber = match.getCurrentMoveNumber();
        ChessGameUtils.setTimeLeftsToMatchResult(matchResult, match, currentMoveNumber);
        match.setResult(matchResult);
        matchRepository.save(match);

        Utils.sendUpdateUsersRatingsRequest(mapper.mapToUpdateUsersRatingsRequest(match));

        return matchResult;
    }
}
