package tsar.alex.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tsar.alex.dto.StartMatchRequest;
import tsar.alex.dto.StartMatchResponse;
import tsar.alex.enums.StartMatchEnum;
import tsar.alex.model.*;
import tsar.alex.repository.MatchRecordRepository;
import tsar.alex.repository.MatchRepository;
import tsar.alex.utils.ChessFactory;
import tsar.alex.utils.Utils;
import tsar.alex.utils.sse.ChessMoveSseEmitters;
import tsar.alex.utils.sse.CustomSseEmitter;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Service
@AllArgsConstructor
public class MatchManagementService {

    private final Map<Long, ChessMoveSseEmitters> emitters;

    private final ThreadLocalRandom threadLocalRandom;
    private final MatchRepository matchRepository;
    private final MatchRecordRepository matchRecordRepository;

    public StartMatchResponse startMatch(StartMatchRequest startMatchRequest) {
        Pair<String> usernames = startMatchRequest.getPairOfUsernames();

        StartMatchResponse response = checkUsersToStartMatch(usernames);

        if (response.getUsersResponses().get(0).getResult()) {
            UsersInMatch usersInMatch;

            if (threadLocalRandom.nextBoolean()) {
                usersInMatch = new UsersInMatch(usernames.get(0), usernames.get(1), ChessColor.WHITE);
            } else {
                usersInMatch = new UsersInMatch(usernames.get(1), usernames.get(0), ChessColor.WHITE);
            }

            Match match = Match.builder().usersInMatch(usersInMatch).boardState(ChessFactory.getInitialBoardState()).build();

            long matchId = matchRepository.save(match).getId();
            saveInitialMatchRecord(matchId);

            response.setMatchId(matchId);

            emitters.put(matchId, new ChessMoveSseEmitters());
        }

        return response;
    }

    public boolean subscribeToMatch(long matchId, CustomSseEmitter emitter) {
        ChessMoveSseEmitters matchSubscribers = emitters.get(matchId);

        if (matchSubscribers == null) {
            emitter.sendErrorAndComplete("No active match with id = " + matchId + " was found.");
            return false;
        }

        String username = Utils.getCurrentUsername();

        matchSubscribers.put(username, emitter);
        return true;
    }


    private void saveInitialMatchRecord(long matchId) {
        MatchRecord matchRecord = MatchRecord.builder().id(matchId).chessMovesRecord(new ArrayList<>()).build();
        matchRecordRepository.save(matchRecord);
    }

    private StartMatchResponse checkUsersToStartMatch(Pair<String> usernames) {

        boolean user0InMatch = matchRepository.isUserInMatch(usernames.get(0));
        boolean user1InMatch = matchRepository.isUserInMatch(usernames.get(1));

        StartMatchEnum user0Response = StartMatchEnum.OK;
        StartMatchEnum user1Response = StartMatchEnum.OK;

        if (user0InMatch) {
            user0Response = StartMatchEnum.ERROR;
            if (user1InMatch) {
                user1Response = StartMatchEnum.ERROR;
            } else {
                user1Response = StartMatchEnum.RETRY;
            }
        } else if (user1InMatch) {
            user1Response = StartMatchEnum.ERROR;
            user0Response = StartMatchEnum.RETRY;
        }

        return new StartMatchResponse(user0Response, user1Response);
    }

}
