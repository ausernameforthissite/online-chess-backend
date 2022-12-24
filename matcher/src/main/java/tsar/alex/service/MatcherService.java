package tsar.alex.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tsar.alex.dto.*;
import tsar.alex.enums.StartMatchEnum;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.model.Pair;
import tsar.alex.model.UserRating;
import tsar.alex.model.UserWaitingForMatch;
import tsar.alex.repository.UserRatingRepository;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static tsar.alex.utils.Constants.PLAYERS_PER_MATCH;

@Service
@RequiredArgsConstructor
public class MatcherService {

    @Resource(name = "usersWaitingForMatchBean")
    private final Set<UserWaitingForMatch> UWFMSet;

    private final ThreadLocalRandom threadLocalRandom;

    private final UserRatingRepository userRatingRepository;

    private final MatcherMapper matcherMapper;


    public boolean initializeUserRating(UserRating userRating) {
        if (userRatingRepository.existsByUserId(userRating.getUserId())) {
            return false;
        } else {
            userRatingRepository.save(userRating);
            return true;
        }
    }

    public FindMatchResult findMatch() {

        UserWaitingForMatch UWFM = new UserWaitingForMatch(Thread.currentThread(), getCurrentUserRating(), null);

        synchronized (UWFMSet) {
            if (UWFMSet.contains(UWFM)) {
                return new FindMatchResult("User is already waiting for match!");
            } else {
                UWFMSet.add(UWFM);
            }
        }

        try {
            Thread.sleep(100_000);
        } catch (InterruptedException e) {
            return UWFM.getResult();
        }

        synchronized (UWFMSet) {
            UWFMSet.remove(UWFM);
        }

        return new FindMatchResult("Match was not found. Try again later");
    }

    private UserRating getCurrentUserRating() {
        Long currentUserId = getCurrentUserId();
        return userRatingRepository.findById(currentUserId).orElseThrow(() -> new UsernameNotFoundException("No rating was found for user with id = " + currentUserId));
    }

    public Long getCurrentUserId() {
        Jwt principal = (Jwt) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return principal.getClaim("user_id");
    }

    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 30 * 1000)
    public void findPairsAndInitiateMatches() {

        UserWaitingForMatch[] UWFMArray;

        synchronized (UWFMSet) {
            if (UWFMSet.size() < 2) {
                return;
            }
            UWFMArray = UWFMSet.toArray(new UserWaitingForMatch[0]);
        }

        Arrays.sort(UWFMArray);

        int upperLimit = UWFMArray.length;
        int lowerLimit = 0;

        if (UWFMArray.length % 2 == 1) {
            int correctionFactor = threadLocalRandom.nextInt(2);
            lowerLimit = correctionFactor;
            upperLimit = upperLimit - 1 + correctionFactor;
        }

        int numberOfPairs = UWFMArray.length / 2;
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(numberOfPairs, 5));

        for (int i = lowerLimit; i < upperLimit; i += 2) {
            Pair<UserWaitingForMatch> usersPair = matcherMapper.mapToUsersPair(UWFMArray[i], UWFMArray[i+1]);
            StartMatchRequest request = matcherMapper.mapToStartMatchRequest(UWFMArray[i], UWFMArray[i+1]);
            executorService.submit(new StartMatchHandler(request, usersPair));
        }

        executorService.shutdown();

        try {
            boolean isTerminated = executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AllArgsConstructor
    private class StartMatchHandler implements Runnable {

        private StartMatchRequest request;
        private Pair<UserWaitingForMatch> usersPair;

        @Override
        public void run() {
            StartMatchResponse response = new RestTemplate().postForObject("http://localhost:8082/api/start_match", new HttpEntity<>(request), StartMatchResponse.class);

            if (response != null) {
                Long matchId = response.getMatchId();

                if (matchId >= 0) {
                    for (int i = 0; i < PLAYERS_PER_MATCH; i++) {
                        UserWaitingForMatch UWFM = usersPair.get(i);

                        synchronized (UWFMSet) {
                            UWFMSet.remove(UWFM);
                        }

                        UWFM.setResult(new FindMatchResult(matchId));
                        UWFM.getThread().interrupt();
                    }
                } else {

                    for (int i = 0; i < PLAYERS_PER_MATCH; i++) {
                        UserWaitingForMatch UWFM = usersPair.get(i);
                        StartMatchEnum result2 = response.getUsersResponses().get(i);

                        if (result2 == StartMatchEnum.ERROR) {

                            synchronized (UWFMSet) {
                                UWFMSet.remove(UWFM);
                            }

                            UWFM.setResult(new FindMatchResult(result2.getMessage()));
                            UWFM.getThread().interrupt();
                        }
                    }
                }
            }

        }
    }
}

