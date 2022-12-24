package tsar.alex.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.StartMatchRequest;
import tsar.alex.dto.StartMatchResponse;
import tsar.alex.enums.StartMatchEnum;
import tsar.alex.model.Match;
import tsar.alex.model.Pair;
import tsar.alex.repository.MatchRepository;

import java.util.concurrent.ThreadLocalRandom;


@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class StartMatchService {

    private final ThreadLocalRandom threadLocalRandom;

    private final MatchRepository matchRepository;

    @Transactional
    public StartMatchResponse startMatch(StartMatchRequest startMatchRequest) {
        Pair<Long> usersIDs = startMatchRequest.getPairOfUsersIds();

        StartMatchResponse response = checkUsersToStartMatch(usersIDs);

        if (response.getUsersResponses().get(0).getResult()) {
            Long whiteUserId;
            Long blackUserId;

            if (threadLocalRandom.nextBoolean()) {
                whiteUserId = usersIDs.get(0);
                blackUserId = usersIDs.get(1);
            } else {
                whiteUserId = usersIDs.get(1);
                blackUserId = usersIDs.get(0);
            }

            Match match = Match.builder().whiteUserId(whiteUserId).blackUserId(blackUserId).isFinished(false).build();
            Match persistentMatch = matchRepository.save(match);
            response.setMatchId(persistentMatch.getId());
        }

        return response;
    }


    private StartMatchResponse checkUsersToStartMatch(Pair<Long> usersIDs) {

        boolean user0InMatch = matchRepository.isUserInMatch(usersIDs.get(0));
        boolean user1InMatch = matchRepository.isUserInMatch(usersIDs.get(1));

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
