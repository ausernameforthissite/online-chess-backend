package tsar.alex.service;


import static tsar.alex.utils.CommonTextConstants.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;
import tsar.alex.dto.response.InitializeUserRatingBadResponse;
import tsar.alex.dto.response.InitializeUserRatingOkResponse;
import tsar.alex.dto.response.InitializeUserRatingResponse;
import tsar.alex.dto.response.UserInGameStatusFalseResponse;
import tsar.alex.dto.response.UserInGameStatusResponse;
import tsar.alex.dto.response.UserInGameStatusTrueResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameBadResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameResponse;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedDatabaseResultException;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.model.*;
import tsar.alex.repository.ChessGameUserRatingsRecordRepository;
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
    private final ChessGameUserRatingsRecordRepository chessGameUserRatingsRecordRepository;

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
    public UserInGameStatusResponse getUserInGameStatus() {
        List<ChessGameUserRatingsRecord> activeGames = findActiveGamesRecordsByUsername(MatcherUtils.getCurrentUsername());

        if (activeGames == null || activeGames.size() == 0) {
            return new UserInGameStatusFalseResponse();
        } else {
            return new UserInGameStatusTrueResponse(activeGames.get(0).getGameId());
        }
    }

    @Transactional(readOnly = true)
    public CurrentUserRating getCurrentUserRating(String username, ChessGameType chessGameType) {
        return currentUserRatingRepository.findById(new CurrentUserRatingId(username, chessGameType)).orElseThrow(() ->
                new DatabaseRecordNotFoundException(String.format(NO_USER_RATING, username, chessGameType.name())));
    }

    @Transactional(readOnly = true)
    public UsersRatingsDataForGameResponse getUsersRatingsDataByGameId(String gameId) {
        Optional<ChessGameUserRatingsRecord> gameUserRatingsRecordOptional = chessGameUserRatingsRecordRepository
                .findById(gameId);
        if (gameUserRatingsRecordOptional.isEmpty()) {
            return new UsersRatingsDataForGameBadResponse(String.format(GAME_ID_NOT_FOUND, gameId));
        }

        return matcherMapper.mapToUsersRatingsDataForGameOkResponse(gameUserRatingsRecordOptional.get());
    }

    @Transactional(readOnly = true)
    public List<ChessGameUserRatingsRecord> findActiveGamesRecordsByUsername(String username) {
        List<ChessGameUserRatingsRecord> activeGames = chessGameUserRatingsRecordRepository
                                                            .findRecordsOfActiveGamesByUsername(username);

        if (activeGames != null && activeGames.size() > 1) {
            throw new UnexpectedDatabaseResultException(String.format(SEVERAL_ACTIVE_GAMES, username));
        }

        return activeGames;
    }

    public void saveChessGameUserRatingsRecord(ChessGameUserRatingsRecord chessGameUserRatingsRecord) {
        chessGameUserRatingsRecordRepository.save(chessGameUserRatingsRecord);
    }

    public void updateAfterGameFinished(UpdateUsersRatingsRequest request) {
        String gameId = request.getGameId();
        ChessGameUserRatingsRecord gameUserRatingsRecord = chessGameUserRatingsRecordRepository
                .findById(gameId).orElseThrow(() -> new RuntimeException(String.format(GAME_ID_NOT_FOUND, gameId)));

        gameUserRatingsRecord.setFinished(true);

        if (request.isTechnicalFinish()) {
            gameUserRatingsRecord.setTechnicalFinish(true);
            return;
        }

        gameUserRatingsRecord.setDraw(request.isDraw());
        gameUserRatingsRecord.setWinnerColor(request.getWinnerColor());
        updateUsersRatings(gameUserRatingsRecord);
    }

    private void updateUsersRatings(ChessGameUserRatingsRecord gameUserRatingsRecord) {
        ChessGameType chessGameType = gameUserRatingsRecord.getChessGameType().getGeneralGameType();
        String[] usernames = {gameUserRatingsRecord.getWhiteUsername(), gameUserRatingsRecord.getBlackUsername()};
        int[] initialRatings = {gameUserRatingsRecord.getWhiteInitialRating(),
                gameUserRatingsRecord.getBlackInitialRating()};

        CurrentUserRating[] currentUserRatings = new CurrentUserRating[2];

        for (int i = 0; i < 2; i++) {
            currentUserRatings[i] = currentUserRatingRepository
                    .findById(new CurrentUserRatingId(usernames[i], chessGameType)).orElseThrow(
                            () -> new RuntimeException("No currentUserRating was found for username = "
                                    + gameUserRatingsRecord.getWhiteUsername()));

            if (currentUserRatings[i].getRating() != gameUserRatingsRecord.getInitialRatingByUserColorIndexNumber(i)) {
                throw new RuntimeException("Initial user ratings in CurrentUserRating ("
                        + currentUserRatings[i].getRating() + ") and in gameUserRatingsRecord ("
                        + gameUserRatingsRecord.getInitialRatingByUserColorIndexNumber(i) + ") for game with gameId = "
                        + gameUserRatingsRecord.getGameId() + "  are not equal!");
            }
        }

        PersonalGameResultEnum[] personalGameResults = new PersonalGameResultEnum[2];

        if (gameUserRatingsRecord.isDraw()) {
            personalGameResults[0] = PersonalGameResultEnum.DRAW;
            personalGameResults[1] = PersonalGameResultEnum.DRAW;
        } else {
            switch (gameUserRatingsRecord.getWinnerColor()) {
                case WHITE:
                    personalGameResults[0] = PersonalGameResultEnum.WIN;
                    personalGameResults[1] = PersonalGameResultEnum.LOSS;
                    break;
                case BLACK:
                    personalGameResults[0] = PersonalGameResultEnum.LOSS;
                    personalGameResults[1] = PersonalGameResultEnum.WIN;
                    break;

                default:
                    throw new RuntimeException("Incorrect winner color: " + gameUserRatingsRecord.getWinnerColor());
            }
        }

        int[] ratingsChanges = new int[2];

        for (int i = 0; i < 2; i++) {
            int currentK = currentUserRatings[i].getK();

            ratingsChanges[i] = EloRating.getRatingChange(initialRatings[i], initialRatings[(i + 1) % 2],
                    currentK, personalGameResults[i]);
            int updatedRating = initialRatings[i] + ratingsChanges[i];

            int gamesPlayed = currentUserRatings[i].incrementGamesPlayed();
            currentUserRatings[i].setK(EloRating.updateK(currentK, updatedRating, gamesPlayed));
            currentUserRatings[i].setRating(updatedRating);
        }

        gameUserRatingsRecord.setWhiteRatingChange(ratingsChanges[0]);
        gameUserRatingsRecord.setBlackRatingChange(ratingsChanges[1]);
    }

}