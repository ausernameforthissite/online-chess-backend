package tsar.alex.service;


import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.UpdateRatingsBadIndividualResultDto;
import tsar.alex.dto.UpdateRatingsIndividualRequestDto;
import tsar.alex.dto.UpdateRatingsIndividualResultDto;
import tsar.alex.dto.UpdateRatingsOkIndividualResultDto;
import tsar.alex.dto.response.UpdateRatingsAfterGameOkResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameResponse;
import tsar.alex.dto.response.UserInGameStatusBadResponse;
import tsar.alex.dto.response.UserInGameStatusFalseResponse;
import tsar.alex.dto.response.UserInGameStatusResponse;
import tsar.alex.dto.response.UserInGameStatusTrueResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameBadResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameResponse;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedDatabaseResultException;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.model.*;
import tsar.alex.repository.ChessGameUserRatingsRecordRepository;
import tsar.alex.repository.CurrentUserRatingRepository;
import tsar.alex.utils.EloRatingCalculator;

import java.util.List;
import java.util.Optional;
import tsar.alex.utils.Utils;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MatcherService {

    private final CurrentUserRatingRepository currentUserRatingRepository;
    private final ChessGameUserRatingsRecordRepository chessGameUserRatingsRecordRepository;
    private final MatcherMapper matcherMapper;

    @Transactional(readOnly = true)
    public UserInGameStatusResponse getUserInGameStatus() {
        String username = Utils.getCurrentUsername();
        List<ChessGameUsersRatingsRecord> activeGames = findActiveGamesRecordsByUsername(Utils.getCurrentUsername());
        log.debug(String.format(USER_ACTIVE_GAMES_LOG, username, activeGames));

        if (activeGames == null) return new UserInGameStatusFalseResponse();

        int numberOfActiveGames = activeGames.size();

        if (numberOfActiveGames == 0) return new UserInGameStatusFalseResponse();

        String[] gamesIds = activeGames.stream().map(ChessGameUsersRatingsRecord::getGameId).toArray(String[]::new);

        if (numberOfActiveGames == 1) return new UserInGameStatusTrueResponse(gamesIds[0]);

        return new UserInGameStatusBadResponse(
                String.format(MULTIPLE_ACTIVE_GAMES, username, Arrays.toString(gamesIds)));
    }

    public void initializeUserRatingsIfNotAlready(String username) {
        if (currentUserRatingRepository.existsByUsernameAndChessGameType(username, ChessGameType.BLITZ)) {
            log.warn(String.format(
                    String.format(RATINGS_ALREADY_INITIALIZED_LOG, username, ChessGameType.BLITZ.name())));
        } else {
            initializeUserRatings(username);
        }
    }

    private void initializeUserRatings(String username) {
        Set<CurrentUserRating> userRatings = new HashSet<>();
        Arrays.stream(ChessGameType.values())
                .forEach(gameType -> userRatings.add(CurrentUserRating.getDefaultUserRating(username, gameType)));
        currentUserRatingRepository.saveAll(userRatings);
        log.debug(String.format(RATINGS_INITIALIZED_LOG, username));
    }

    @Transactional(readOnly = true)
    public CurrentUserRating getCurrentUserRating(String username, ChessGameType chessGameType) {
        return currentUserRatingRepository.findById(new CurrentUserRatingId(username, chessGameType)).orElseThrow(() ->
                new DatabaseRecordNotFoundException(String.format(NO_USER_RATING, username, chessGameType.name())));
    }

    @Transactional(readOnly = true)
    public UsersRatingsDataForGameResponse getUsersRatingsDataByGameId(String gameId) {
        Optional<ChessGameUsersRatingsRecord> gameUserRatingsRecordOptional = chessGameUserRatingsRecordRepository
                .findById(gameId);
        if (gameUserRatingsRecordOptional.isEmpty()) {
            return new UsersRatingsDataForGameBadResponse(String.format(GAME_ID_NOT_FOUND, gameId));
        }

        return matcherMapper.mapToUsersRatingsDataForGameOkResponse(gameUserRatingsRecordOptional.get());
    }

    @Transactional(readOnly = true)
    public List<ChessGameUsersRatingsRecord> findActiveGamesRecordsByUsername(String username) {
        List<ChessGameUsersRatingsRecord> activeGames = chessGameUserRatingsRecordRepository
                .findRecordsOfActiveGamesByUsername(username);

        if (activeGames != null && activeGames.size() > 1) {
            String[] gameIds = activeGames.stream().map(ChessGameUsersRatingsRecord::getGameId).toArray(String[]::new);
            throw new UnexpectedDatabaseResultException(
                    String.format(MULTIPLE_ACTIVE_GAMES, username, String.join(" ,", gameIds)));
        }

        return activeGames;
    }

    public boolean saveChessGameUserRatingsRecordIfNotAlready(ChessGameUsersRatingsRecord newRecord) {
        Optional<ChessGameUsersRatingsRecord> persistentRecordOptional = chessGameUserRatingsRecordRepository.findById(
                newRecord.getGameId());

        if (persistentRecordOptional.isPresent()) {
            ChessGameUsersRatingsRecord persistentRecord = persistentRecordOptional.get();

            if (persistentRecord.equals(newRecord)) {
                return true;
            }

            log.error(String.format(DB_AND_RAM_OBJECT_NOT_CORRESPOND, persistentRecord, newRecord));
            return false;
        }

        saveChessGameUserRatingsRecord(newRecord);
        return true;
    }

    public void saveChessGameUserRatingsRecord(ChessGameUsersRatingsRecord newRecord) {
        chessGameUserRatingsRecordRepository.save(newRecord);
    }

    public UpdateRatingsAfterGameResponse updateRatingsAfterGame(
            List<UpdateRatingsIndividualRequestDto> individualRequests) {

        int numberOfIndividualRequests = individualRequests.size();
        List<String> gamesIds = individualRequests.stream().map(UpdateRatingsIndividualRequestDto::getGameId).toList();
        List<UpdateRatingsIndividualResultDto> resultList = Stream.generate(
                        () -> (UpdateRatingsIndividualResultDto) null).limit(numberOfIndividualRequests)
                .collect(Collectors.toList());

        List<ChessGameUsersRatingsRecord> gameUsersRatingsRecords = chessGameUserRatingsRecordRepository.findAllByGameIdIn(
                gamesIds);
        List<String> foundGamesIds = gameUsersRatingsRecords.stream().map(ChessGameUsersRatingsRecord::getGameId)
                .toList();

        for (int i = 0; i < numberOfIndividualRequests; i++) {

            String gameId = gamesIds.get(i);
            int index = foundGamesIds.indexOf(gameId);

            UpdateRatingsIndividualResultDto individualResult;

            if (index >= 0) {
                individualResult = updateIndividualRatings(gameUsersRatingsRecords.get(index),
                        individualRequests.get(index));
            } else {
                String message = String.format(NO_RATINGS_RECORD_FOR_GAME_ID, gameId);
                log.error(message);
                individualResult = new UpdateRatingsBadIndividualResultDto(message);
            }

            resultList.add(i, individualResult);
        }

        return new UpdateRatingsAfterGameOkResponse(resultList);
    }


    private UpdateRatingsIndividualResultDto updateIndividualRatings(ChessGameUsersRatingsRecord gameUsersRatingsRecord,
            UpdateRatingsIndividualRequestDto individualRequest) {

        log.debug(String.format(OBJECT_BEFORE_UPDATE_LOG, "Game users ratings record", gameUsersRatingsRecord));
        gameUsersRatingsRecord.setFinished(true);

        if (individualRequest.isTechnicalFinish()) {
            gameUsersRatingsRecord.setTechnicalFinish(true);
            log.debug(String.format(TECHNICAL_FINISH_LOG, gameUsersRatingsRecord.getGameId()));
            return new UpdateRatingsOkIndividualResultDto();
        }

        gameUsersRatingsRecord.setDraw(individualRequest.isDraw());
        gameUsersRatingsRecord.setWinnerColor(individualRequest.getWinnerColor());

        CurrentUserRating whiteUserRating;
        CurrentUserRating blackUserRating;
        PersonalGameResultEnum whitePersonalGameResult;
        PersonalGameResultEnum blackPersonalGameResult;

        try {
            whiteUserRating = retrieveCurrentUserRating(gameUsersRatingsRecord, ChessColor.WHITE);
            blackUserRating = retrieveCurrentUserRating(gameUsersRatingsRecord, ChessColor.BLACK);
            whitePersonalGameResult = determinePersonalGameResult(gameUsersRatingsRecord, ChessColor.WHITE);
            blackPersonalGameResult = determinePersonalGameResult(gameUsersRatingsRecord, ChessColor.BLACK);
        } catch (RuntimeException e) {
            log.error(e.toString());
            return new UpdateRatingsBadIndividualResultDto(e.getMessage());
        }

        int whiteRatingChange = updateCurrentUserRatingAndGetRatingChange(gameUsersRatingsRecord,
                whiteUserRating, whitePersonalGameResult);
        int blackRatingChange = updateCurrentUserRatingAndGetRatingChange(gameUsersRatingsRecord,
                blackUserRating, blackPersonalGameResult);

        gameUsersRatingsRecord.setWhiteRatingChange(whiteRatingChange);
        gameUsersRatingsRecord.setBlackRatingChange(blackRatingChange);
        log.debug(String.format(OBJECT_AFTER_UPDATE_LOG, "Game users ratings record", gameUsersRatingsRecord));
        return new UpdateRatingsOkIndividualResultDto();
    }

    private CurrentUserRating retrieveCurrentUserRating(ChessGameUsersRatingsRecord gameUsersRatingsRecord,
            ChessColor userColor) {
        String username = gameUsersRatingsRecord.getUsernameByUserColor(userColor);
        ChessGameType generalGameType = gameUsersRatingsRecord.getChessGameType().getGeneralGameType();

        CurrentUserRating currentUserRating = currentUserRatingRepository.findById(
                new CurrentUserRatingId(username, generalGameType)).orElseThrow(
                () -> new UnexpectedDatabaseResultException(
                        String.format(NO_USER_RATING, username, generalGameType)));

        log.debug(String.format(OBJECT_BEFORE_UPDATE_LOG, "Current user rating", currentUserRating));

        int ratingFromCurrentUserRating = currentUserRating.getRating();
        int ratingFromGameUsersRatingsRecord = gameUsersRatingsRecord.getInitialRatingByUserColor(userColor);

        if (ratingFromCurrentUserRating != ratingFromGameUsersRatingsRecord) {
            throw new RuntimeException(String.format(RATINGS_NOT_CORRESPOND, ratingFromCurrentUserRating,
                    ratingFromGameUsersRatingsRecord, gameUsersRatingsRecord.getGameId()));
        }

        return currentUserRating;
    }

    private PersonalGameResultEnum determinePersonalGameResult(ChessGameUsersRatingsRecord gameUsersRatingsRecord,
            ChessColor userColor) {

        if (gameUsersRatingsRecord.isDraw()) return PersonalGameResultEnum.DRAW;

        ChessColor winnerColor = gameUsersRatingsRecord.getWinnerColor();
        if (winnerColor == userColor) return PersonalGameResultEnum.WIN;
        if (winnerColor == ChessColor.getInvertedColor(userColor)) return PersonalGameResultEnum.LOSS;

        throw new IllegalArgumentException(
                String.format(ILLEGAL_ARGUMENT, "gameUsersRatingsRecord", gameUsersRatingsRecord));
    }

    private int updateCurrentUserRatingAndGetRatingChange(ChessGameUsersRatingsRecord gameUsersRatingsRecord,
            CurrentUserRating currentUserRating, PersonalGameResultEnum personalGameResult) {

        ChessColor userColor = gameUsersRatingsRecord.getUserColorByUsername(currentUserRating.getUsername());
        int enemyInitialRating = gameUsersRatingsRecord.getInitialRatingByUserColor(
                ChessColor.getInvertedColor(userColor));
        int userInitialRating = currentUserRating.getRating();
        int currentK = currentUserRating.getK();
        int ratingChange = EloRatingCalculator.calculateRatingChange(userInitialRating, enemyInitialRating, currentK,
                personalGameResult);
        int updatedRating = userInitialRating + ratingChange;
        int gamesPlayed = currentUserRating.incrementGamesPlayed();
        currentUserRating.setK(EloRatingCalculator.updateK(currentK, updatedRating, gamesPlayed));
        currentUserRating.setRating(updatedRating);

        log.debug(String.format(OBJECT_AFTER_UPDATE_LOG, "Current user rating", currentUserRating));
        return ratingChange;

    }

}
