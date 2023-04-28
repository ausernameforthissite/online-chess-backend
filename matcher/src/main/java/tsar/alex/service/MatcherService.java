package tsar.alex.service;


import static tsar.alex.utils.CommonTextConstants.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;
import tsar.alex.dto.response.InitializeUserRatingBadResponse;
import tsar.alex.dto.response.InitializeUserRatingOkResponse;
import tsar.alex.dto.response.InitializeUserRatingResponse;
import tsar.alex.dto.response.UserInMatchStatusFalseResponse;
import tsar.alex.dto.response.UserInMatchStatusResponse;
import tsar.alex.dto.response.UserInMatchStatusTrueResponse;
import tsar.alex.dto.response.UsersRatingsDataForMatchBadResponse;
import tsar.alex.dto.response.UsersRatingsDataForMatchResponse;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedDatabaseResultException;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.model.*;
import tsar.alex.repository.ChessMatchUserRatingsRecordRepository;
import tsar.alex.repository.CurrentUserRatingRepository;
import tsar.alex.utils.EloRating;

import java.util.List;
import java.util.Optional;
import tsar.alex.utils.MatcherUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class MatcherService {


    private final CurrentUserRatingRepository currentUserRatingRepository;
    private final ChessMatchUserRatingsRecordRepository chessMatchUserRatingsRecordRepository;

    private final MatcherMapper matcherMapper;


    public InitializeUserRatingResponse initializeUserRating(String username) {
        if (currentUserRatingRepository.existsByUsername(username)) {
            return new InitializeUserRatingBadResponse(String.format(ALREADY_EXISTS, username));
        } else {
            for (ChessGameType chessGameType : ChessGameType.values()) {
                currentUserRatingRepository.save(CurrentUserRating.getDefaultUserRating(username, chessGameType));
            }
            return new InitializeUserRatingOkResponse();
        }
    }

    @Transactional(readOnly = true)
    public UserInMatchStatusResponse getUserInMatchStatus() {
        List<ChessMatchUserRatingsRecord> activeMatches = findActiveMatchesRecordsByUsername(MatcherUtils.getCurrentUsername());

        if (activeMatches == null || activeMatches.size() == 0) {
            return new UserInMatchStatusFalseResponse();
        } else {
            return new UserInMatchStatusTrueResponse(activeMatches.get(0).getMatchId());
        }
    }

    @Transactional(readOnly = true)
    public CurrentUserRating getCurrentUserRating(String username, ChessGameType chessGameType) {
        return currentUserRatingRepository.findById(new CurrentUserRatingId(username, chessGameType)).orElseThrow(() ->
                new DatabaseRecordNotFoundException(String.format(NO_USER_RATING, username, chessGameType.name())));
    }

    @Transactional(readOnly = true)
    public UsersRatingsDataForMatchResponse getUsersRatingsDataByMatchId(String matchId) {
        Optional<ChessMatchUserRatingsRecord> matchUserRatingsRecordOptional = chessMatchUserRatingsRecordRepository
                .findById(matchId);
        if (matchUserRatingsRecordOptional.isEmpty()) {
            return new UsersRatingsDataForMatchBadResponse(String.format(MATCH_ID_NOT_FOUND, matchId));
        }

        return matcherMapper.mapToUsersRatingsDataForMatchOkResponse(matchUserRatingsRecordOptional.get());
    }

    @Transactional(readOnly = true)
    public List<ChessMatchUserRatingsRecord> findActiveMatchesRecordsByUsername(String username) {
        List<ChessMatchUserRatingsRecord> activeMatches = chessMatchUserRatingsRecordRepository
                                                            .findRecordsOfActiveMatchesByUsername(username);

        if (activeMatches != null && activeMatches.size() > 1) {
            throw new UnexpectedDatabaseResultException(String.format(SEVERAL_ACTIVE_MATCHES, username));
        }

        return activeMatches;
    }

    public void saveChessMatchUserRatingsRecord(ChessMatchUserRatingsRecord chessMatchUserRatingsRecord) {
        chessMatchUserRatingsRecordRepository.save(chessMatchUserRatingsRecord);
    }

    public void updateAfterMatchFinished(UpdateUsersRatingsRequest request) {
        String matchId = request.getMatchId();
        ChessMatchUserRatingsRecord matchUserRatingsRecord = chessMatchUserRatingsRecordRepository
                .findById(matchId).orElseThrow(() -> new RuntimeException(String.format(MATCH_ID_NOT_FOUND, matchId)));

        matchUserRatingsRecord.setFinished(true);

        if (request.isTechnicalFinish()) {
            matchUserRatingsRecord.setTechnicalFinish(true);
            return;
        }

        matchUserRatingsRecord.setDraw(request.isDraw());
        matchUserRatingsRecord.setWinnerColor(request.getWinnerColor());
        updateUsersRatings(matchUserRatingsRecord);
    }

    private void updateUsersRatings(ChessMatchUserRatingsRecord matchUserRatingsRecord) {
        ChessGameType chessGameType = matchUserRatingsRecord.getChessGameType().getGeneralGameType();
        String[] usernames = {matchUserRatingsRecord.getWhiteUsername(), matchUserRatingsRecord.getBlackUsername()};
        int[] initialRatings = {matchUserRatingsRecord.getWhiteInitialRating(),
                matchUserRatingsRecord.getBlackInitialRating()};

        CurrentUserRating[] currentUserRatings = new CurrentUserRating[2];

        for (int i = 0; i < 2; i++) {
            currentUserRatings[i] = currentUserRatingRepository
                    .findById(new CurrentUserRatingId(usernames[i], chessGameType)).orElseThrow(
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

