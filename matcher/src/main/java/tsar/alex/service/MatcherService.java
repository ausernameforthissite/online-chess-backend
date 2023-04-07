package tsar.alex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.UpdateUsersRatingsRequest;
import tsar.alex.dto.UsersRatingsDataForMatchBadResponse;
import tsar.alex.dto.UsersRatingsDataForMatchResponse;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.model.*;
import tsar.alex.repository.ChessMatchUserRatingsRecordRepository;
import tsar.alex.repository.CurrentUserRatingRepository;
import tsar.alex.utils.EloRating;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MatcherService {

    private final CurrentUserRatingRepository currentUserRatingRepository;
    private final ChessMatchUserRatingsRecordRepository chessMatchUserRatingsRecordRepository;

    private final MatcherMapper matcherMapper;



    public boolean initializeUserRating(String username) {
        if (currentUserRatingRepository.existsByUsername(username)) {
            return false;
        } else {
            currentUserRatingRepository.save(matcherMapper.mapToDefaultCurrentUserRating(username));
            return true;
        }
    }

    @Transactional(readOnly = true)
    public CurrentUserRating getCurrentUserRating(String username) {
        return currentUserRatingRepository.findById(username).orElseThrow(() ->
                new UsernameNotFoundException("No rating was found for user with name = " + username));
    }

    public UsersRatingsDataForMatchResponse getUsersRatingsDataByMatchId(long matchId) {
        Optional<ChessMatchUserRatingsRecord> matchUserRatingsRecordOptional = chessMatchUserRatingsRecordRepository
                                                                                            .findById(matchId);
        if (matchUserRatingsRecordOptional.isEmpty()) {
            return new UsersRatingsDataForMatchBadResponse("Record of match with id = " + matchId + " was not found.");
        }

        return matcherMapper.mapToUsersRatingsDataForMatchOkResponse(matchUserRatingsRecordOptional.get());
    }

    public List<ChessMatchUserRatingsRecord> findActiveMatchesRecordsByUsername(String username) {
        return chessMatchUserRatingsRecordRepository.findRecordsOfActiveMatchesByUsername(username);
    }

    public void saveChessMatchUserRatingsRecord(ChessMatchUserRatingsRecord chessMatchUserRatingsRecord) {
        chessMatchUserRatingsRecordRepository.save(chessMatchUserRatingsRecord);
    }

    public boolean updateAfterMatchFinished(UpdateUsersRatingsRequest request) {
        ChessMatchUserRatingsRecord matchUserRatingsRecord = chessMatchUserRatingsRecordRepository
                .findById(request.getMatchId()).orElseThrow(() -> new RuntimeException(
                        "No matchUserRatingsRecord was found for match with id = " + request.getMatchId()));

        matchUserRatingsRecord.setFinished(true);

        if (request.isTechnicalFinish()) {
            matchUserRatingsRecord.setTechnicalFinish(true);
            return true;
        }

        matchUserRatingsRecord.setDraw(request.isDraw());
        matchUserRatingsRecord.setWinnerColor(request.getWinnerColor());
        updateUsersRatings(matchUserRatingsRecord);
        return true;
    }

    private void updateUsersRatings(ChessMatchUserRatingsRecord matchUserRatingsRecord) {
        String[] usernames = {matchUserRatingsRecord.getWhiteUsername(), matchUserRatingsRecord.getBlackUsername()};
        int[] initialRatings = {matchUserRatingsRecord.getWhiteInitialRating(),
                                matchUserRatingsRecord.getBlackInitialRating()};

        CurrentUserRating[] currentUserRatings = new CurrentUserRating[2];

        for (int i = 0; i < 2; i++) {
            currentUserRatings[i] = currentUserRatingRepository
                    .findById(usernames[i]).orElseThrow(
                            () -> new RuntimeException("No currentUserRating was found for username = "
                                    + matchUserRatingsRecord.getWhiteUsername()));

            if (currentUserRatings[i].getRating() != matchUserRatingsRecord.getInitialRatingByUserColorIndexNumber(i)) {
                throw new RuntimeException("Initial user ratings in CurrentUserRating ("
                        + currentUserRatings[i].getRating() + ") and in matchUserRatingsRecord ("
                        + matchUserRatingsRecord.getInitialRatingByUserColorIndexNumber(i) + ") for match with id = "
                        + matchUserRatingsRecord.getMatchId() + "  are not equal!");
            }
        }


        PersonalMatchResultEnum[] personalMatchResults = new PersonalMatchResultEnum[2];

        if (matchUserRatingsRecord.isDraw()) {
            personalMatchResults[0] = PersonalMatchResultEnum.DRAW;
            personalMatchResults[1] = PersonalMatchResultEnum.DRAW;
        } else {
            switch (matchUserRatingsRecord.getWinnerColor()) {
                case WHITE:
                    personalMatchResults[0] = PersonalMatchResultEnum.WIN;
                    personalMatchResults[1] = PersonalMatchResultEnum.LOSS;
                    break;
                case BLACK:
                    personalMatchResults[0] = PersonalMatchResultEnum.LOSS;
                    personalMatchResults[1] = PersonalMatchResultEnum.WIN;
                    break;

                default:
                    throw new RuntimeException("Incorrect winner color: " + matchUserRatingsRecord.getWinnerColor());
            }
        }


        int[] ratingsChanges = new int[2];

        for (int i = 0; i < 2; i++) {
            int currentK = currentUserRatings[i].getK();

            ratingsChanges[i] = EloRating.getRatingChange(initialRatings[i], initialRatings[(i + 1) % 2],
                                                        currentK, personalMatchResults[i]);
            int updatedRating = initialRatings[i] + ratingsChanges[i];

            int matchesPlayed = currentUserRatings[i].incrementMatchesPlayed();
            currentUserRatings[i].setK(EloRating.updateK(currentK, updatedRating, matchesPlayed));
            currentUserRatings[i].setRating(updatedRating);
        }

        matchUserRatingsRecord.setWhiteRatingChange(ratingsChanges[0]);
        matchUserRatingsRecord.setBlackRatingChange(ratingsChanges[1]);
    }

}

