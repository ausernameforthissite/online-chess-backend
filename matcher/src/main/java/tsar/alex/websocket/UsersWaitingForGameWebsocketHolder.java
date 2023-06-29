package tsar.alex.websocket;

import static tsar.alex.model.ChessGameTypeWithTimings.*;
import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Constants.MAX_THREADS_IN_THREAD_POOL;

import java.time.Instant;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import tsar.alex.api.WebsocketMessageSender;
import tsar.alex.api.client.MatcherRestClient;
import tsar.alex.dto.StartGameAlreadyInGamePersonalResultDto;
import tsar.alex.dto.StartGameBadEnemyPersonalResultDto;
import tsar.alex.dto.StartGameMultipleActiveGamesPersonalResultDto;
import tsar.alex.dto.StartGameOkPersonalResultDto;
import tsar.alex.dto.StartGamePersonalResultDto;
import tsar.alex.dto.WebsocketResponse;
import tsar.alex.dto.request.CheckRegisteredRequest;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.response.CheckRegisteredBadResponse;
import tsar.alex.dto.response.CheckRegisteredOkResponse;
import tsar.alex.dto.response.CheckRegisteredResponse;
import tsar.alex.dto.response.StartGameBadResponse;
import tsar.alex.dto.response.StartGameOkResponse;
import tsar.alex.dto.response.StartGameResponse;
import tsar.alex.dto.websocket.response.*;
import tsar.alex.dto.websocket.response.FindGameWebsocketResponseEnum.FindGameWebsocketBadResponseEnum;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedDatabaseResultException;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.*;
import tsar.alex.service.MatcherService;
import tsar.alex.utils.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsersWaitingForGameWebsocketHolder {

    private final static Set<ChessGameTypeWithTimings> BLITZ_GAMES = Set.of(BLITZ_3_0, BLITZ_3_2);
    private final static Set<ChessGameTypeWithTimings> BULLET_AND_RAPID_GAMES = Set.of(BULLET_1_0, BULLET_1_2,
            RAPID_10_0, RAPID_10_5);
    private final static Set<ChessGameTypeWithTimings> CLASSIC_GAMES = Set.of(CLASSIC_30_0, CLASSIC_30_30);

    private final Map<String, UserWaitingForGame> uwfgMap = new HashMap<>();
    private final ThreadLocalRandom threadLocalRandom;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MatcherService matcherService;
    private final MatcherRestClient matcherRestClient;
    private final WebsocketMessageSender websocketMessageSender;


    public void addUserIfPossible(String username, ChessGameTypeWithTimings searchGameType,
            WebsocketSessionWrapper websocketSessionWrapper) {
        WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper);

        if (!matcherRestClient.isGameMasterMicroserviceAvailable()) {
            throw new WebsocketException(SERVICE_UNAVAILABLE, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        checkActiveGames(username);

        ChessGameType generalGameType = searchGameType.getGeneralGameType();
        CurrentUserRating userRating = matcherService.getCurrentUserRating(username, generalGameType);

        if (userRating == null) {
            checkRegistrationAndInitializeRatings(username, generalGameType);
        }

        UserWaitingForGame uwfg;
        UserWaitingForGame oldUwfg;

        synchronized (uwfgMap) {
            oldUwfg = uwfgMap.get(username);

            if (oldUwfg != null) {
                WebSocketSession session = oldUwfg.getSessionWrapper().getSession();

                if (session != null && session.isOpen()) {
                    throw new WebsocketException(ALREADY_SEARCHING, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }

                log.debug(String.format(NO_OPENED_SESSION_LOG, username));
                uwfg = oldUwfg;
            } else {
                uwfg = UserWaitingForGame.builder().currentUserRating(userRating).searchGameType(searchGameType)
                        .build();
            }

            ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(
                    new TimeoutSearchAbortHandler(username), MatcherConstants.GAME_SEARCH_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutDisconnectTask(timeoutDisconnectTask);
            uwfg.setSessionWrapper(websocketSessionWrapper);
            uwfgMap.put(username, uwfg);
        }
    }

    private void checkActiveGames(String username) {
        try {
            List<ChessGameUsersRatingsRecord> activeGames = matcherService.findActiveGamesRecordsByUsername(
                    username);

            if (activeGames != null && activeGames.size() == 1) {
                throw new WebsocketException(String.format(ALREADY_IN_GAME, activeGames.get(0).getGameId()),
                        WebsocketErrorCodeEnum.CLOSE_CONNECTION_ALREADY_IN_GAME);
            }
        } catch (UnexpectedDatabaseResultException e) {
            throw new WebsocketException(e, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }
    }

    private void checkRegistrationAndInitializeRatings(String username, ChessGameType gameType) {
        log.warn(String.format(NO_RATING_FOR_USER_AND_GAME_TYPE_LOG, username, gameType.name()));

        CheckRegisteredResponse checkRegisteredResult = matcherRestClient.checkIfUserIsRegistered(
                new CheckRegisteredRequest(username));

        if (checkRegisteredResult instanceof CheckRegisteredOkResponse) {
            matcherService.initializeUserRatingsIfNotAlready(username);
        } else if (checkRegisteredResult instanceof CheckRegisteredBadResponse badResponse) {
            log.error(badResponse.getMessage());
            throw new WebsocketException(SERVER_ERROR, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        } else {
            throw new UnexpectedObjectClassException(String.format(UNEXPECTED_OBJECT_CLASS, "checkRegisteredResult",
                    checkRegisteredResult.getClass().getName()));
        }
    }

    public void cancelSearchIfPossible(String username) {
        UserWaitingForGame uwfg;

        synchronized (uwfgMap) {
            uwfg = uwfgMap.get(username);
            if (uwfg != null && uwfg.isWaitingForAnswer()) {
                WebsocketResponse response = new FindGameBadResponse(GAME_BEING_CREATED,
                        FindGameWebsocketBadResponseEnum.CANCEL_BAD);
                WebSocketSession session = uwfg.getSessionWrapper().getSession();
                websocketMessageSender.sendMessageToWebsocketSession(response, session);
                return;
            }
            uwfgMap.remove(username);
        }

        if (uwfg != null) {
            WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();

            WebsocketResponse response = new FindGameCancelOkResponse();
            WebSocketSession session = websocketSessionWrapper.getSession();
            websocketMessageSender.sendMessageToWebsocketSession(response, session);

            WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper);
            setAfterSearchFinishedTimeoutDisconnectTask(websocketSessionWrapper);
        } else {
            log.debug(String.format(ALREADY_NOT_SEARCHING_LOG, username));
        }
    }

    public void abortAllSearches() {
        Set<Entry<String, UserWaitingForGame>> uwfgEntrySet = new HashSet<>();

        synchronized (uwfgMap) {
            log.debug(String.format(UWFG_MAP_BEFORE_REMOVE_LOG, uwfgMap));

            for (Iterator<Entry<String, UserWaitingForGame>> iterator = uwfgMap.entrySet().iterator();
                    iterator.hasNext(); ) {
                Entry<String, UserWaitingForGame> entry = iterator.next();
                if (!entry.getValue().isWaitingForAnswer()) {
                    uwfgEntrySet.add(entry);
                    iterator.remove();
                }
            }

            log.debug(String.format(UWFG_MAP_AFTER_REMOVE_LOG, uwfgMap));
        }

        log.debug(String.format(REMOVED_ENTRIES_LOG, uwfgEntrySet));

        if (uwfgEntrySet.size() > 0) {
            String websocketResponseAsJsonString = websocketMessageSender.mapResponseToJsonString(
                    new FindGameBadResponse(SERVER_ERROR, FindGameWebsocketBadResponseEnum.FIND_GAME_BAD));
            ExecutorService executorService = Executors.newFixedThreadPool(
                    Math.min(uwfgEntrySet.size(), MAX_THREADS_IN_THREAD_POOL));
            uwfgEntrySet.forEach((uwfgEntry) -> executorService.submit(
                    () -> handleUserSearchAbort(uwfgEntry.getKey(), uwfgEntry.getValue(),
                            websocketResponseAsJsonString)));
            executorService.shutdown();
        }
    }

    private void abortSearchOnTimeoutIfPossible(String username) {
        UserWaitingForGame uwfg;

        synchronized (uwfgMap) {
            uwfg = uwfgMap.get(username);
            if (uwfg != null && uwfg.isWaitingForAnswer()) {
                return;
            }
            uwfgMap.remove(username);
        }

        handleUserSearchAbort(CANNOT_FIND_ENEMY, username, uwfg);
    }

    private void handleUserSearchAbort(String errorMessage, String username, UserWaitingForGame uwfg) {
        String websocketResponseAsJsonString = websocketMessageSender.mapResponseToJsonString(
                new FindGameBadResponse(errorMessage, FindGameWebsocketBadResponseEnum.FIND_GAME_BAD));
        handleUserSearchAbort(username, uwfg, websocketResponseAsJsonString);
    }

    private void handleUserSearchAbort(String username, UserWaitingForGame uwfg, String websocketResponseAsJsonString) {
        if (uwfg != null) {
            WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();
            WebSocketSession session = websocketSessionWrapper.getSession();
            websocketMessageSender.sendMessageToWebsocketSession(websocketResponseAsJsonString, session);
            WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper, false);
            setAfterSearchFinishedTimeoutDisconnectTask(websocketSessionWrapper);
        } else {
            log.debug(String.format(ALREADY_NOT_SEARCHING_LOG, username));
        }
    }

    private void setAfterSearchFinishedTimeoutDisconnectTask(WebsocketSessionWrapper websocketSessionWrapper) {
        WebSocketSession websocketSession = websocketSessionWrapper.getSession();
        if (websocketSession != null) {
            ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(
                    new TimeoutWebsocketCloseHandler(websocketSession), Constants.SESSION_TO_BE_CLOSED_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutDisconnectTask(timeoutDisconnectTask);
        }
    }

    public void removeDisconnectedUserIfPossible(String username, String sessionId) {
        UserWaitingForGame uwfg;

        synchronized (uwfgMap) {
            uwfg = uwfgMap.get(username);

            if (uwfg != null) {
                if (uwfg.isWaitingForAnswer()) return;

                WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();
                WebSocketSession websocketSession = websocketSessionWrapper.getSession();

                if (websocketSession != null) {
                    String sessionIdFromUwfgMap = websocketSession.getId();
                    if (!sessionId.equals(sessionIdFromUwfgMap)) {
                        log.debug(String.format(DIFFERENT_SESSION_IDS, sessionId, sessionIdFromUwfgMap));
                        return;
                    }
                }

                // In case of new disconnect task was assigned while this method was running
                WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper);
            }
            uwfgMap.remove(username);
        }
    }

    @Scheduled(fixedDelay = 15 * 1000, initialDelay = 15 * 1000)
    private void startBlitzGames() {
        startGamesOfCertainTypes(BLITZ_GAMES);
    }

    @Scheduled(fixedDelay = 20 * 1000, initialDelay = 20 * 1000)
    private void startBulletAndRapidGames() {
        startGamesOfCertainTypes(BULLET_AND_RAPID_GAMES);
    }

    @Scheduled(fixedDelay = 70 * 1000, initialDelay = 70 * 1000)
    private void startClassicGames() {
        startGamesOfCertainTypes(CLASSIC_GAMES);
    }

    private void startGamesOfCertainTypes(Set<ChessGameTypeWithTimings> gameTypes) {
        for (ChessGameTypeWithTimings gameType : gameTypes) {
            List<UserWaitingForGame> uwfgList;

            synchronized (uwfgMap) {
                log.trace(String.format(UWFG_MAP_BEFORE_START_GAMES_LOG, gameType, uwfgMap));

                if (uwfgMap.size() < 2) {
                    return;
                }

                uwfgList = uwfgMap.values().stream()
                        .filter(uwfg -> !uwfg.isWaitingForAnswer() && uwfg.getSearchGameType() == gameType)
                        .collect(Collectors.toList());
                int listSize = uwfgList.size();

                if (listSize < 2) {
                    continue;
                }

                if (listSize % 2 == 1) {
                    uwfgList.remove(threadLocalRandom.nextInt(listSize));
                }
                uwfgList.forEach(uwfg -> uwfg.setWaitingForAnswer(true));
            }

            log.debug(String.format(UWFG_LIST_TO_START_GAMES_LOG, gameType, uwfgList));
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new StartGamesHandler(gameType, uwfgList));
            executorService.shutdown();
        }
    }

    private class StartGamesHandler implements Runnable {

        private final ChessGameTypeWithTimings gameType;
        private final List<UserWaitingForGame> uwfgList;
        private List<String> usernames;
        private Instant startedAt;
        private final ReentrantLock saveChessGameUserRatingsRecordLock = new ReentrantLock();

        public StartGamesHandler(ChessGameTypeWithTimings gameType, List<UserWaitingForGame> uwfgList) {
            this.gameType = gameType;
            this.uwfgList = uwfgList;
        }

        @Override
        public void run() {
            Collections.sort(uwfgList);

            usernames = uwfgList.stream().map(uwfg -> uwfg.getCurrentUserRating().getUsername())
                    .collect(Collectors.toList());

            StartGameResponse startGameResponse = matcherRestClient.startGames(
                    new StartGameRequest(gameType, usernames));

            if (startGameResponse instanceof StartGameBadResponse badResponse) {
                handleStartGameBadResponse(badResponse);
            } else if (startGameResponse instanceof StartGameOkResponse okResponse) {
                handleStartGameOkResponse(okResponse);
            } else {
                throw new UnexpectedObjectClassException(
                        String.format(UNEXPECTED_OBJECT_CLASS, "startGameResponse", startGameResponse.getClass()));
            }
        }

        private void handleStartGameBadResponse(StartGameBadResponse badResponse) {
            log.error(badResponse.getMessage());

            String websocketResponseAsJsonString = websocketMessageSender.mapResponseToJsonString(
                    new FindGameBadResponse(SERVER_ERROR, FindGameWebsocketBadResponseEnum.FIND_GAME_BAD));
            ExecutorService executorService = Executors.newFixedThreadPool(
                    Math.min(uwfgList.size(), MAX_THREADS_IN_THREAD_POOL));
            uwfgList.forEach((uwfg) -> executorService.submit(
                    () -> handleUserSearchAbort(uwfg.getCurrentUserRating().getUsername(), uwfg,
                            websocketResponseAsJsonString)));
            executorService.shutdown();

            synchronized (uwfgMap) {
                log.debug(String.format(UWFG_MAP_BEFORE_REMOVE_LOG, uwfgMap));
                usernames.forEach(uwfgMap::remove);
                log.debug(String.format(UWFG_MAP_AFTER_REMOVE_LOG, uwfgMap));
            }
        }

        private void handleStartGameOkResponse(StartGameOkResponse okResponse) {
            startedAt = okResponse.getStartedAt();
            List<StartGamePersonalResultDto> personalResults = okResponse.getPersonalResults();

            ExecutorService executorService = Executors.newFixedThreadPool(
                    Math.min(personalResults.size(), MAX_THREADS_IN_THREAD_POOL));
            personalResults.forEach(
                    (personalResult) -> executorService.submit(() -> handleStartGamePersonalResult(personalResult)));
            executorService.shutdown();
        }

        private void handleStartGamePersonalResult(StartGamePersonalResultDto personalResult) {
            if (personalResult instanceof StartGameOkPersonalResultDto okResult) {
                new OkPersonalResultHandler(okResult).handleOkPersonalResult();
            } else if (personalResult instanceof StartGameBadEnemyPersonalResultDto badEnemyResult) {
                handleBadEnemyPersonalResult(badEnemyResult);
            } else if (personalResult instanceof StartGameAlreadyInGamePersonalResultDto alreadyInGameResult) {
                handleAlreadyInGamePersonalResult(alreadyInGameResult);
            } else if (personalResult instanceof StartGameMultipleActiveGamesPersonalResultDto multipleActiveGamesResult) {
                handleMultipleActiveGamesResult(multipleActiveGamesResult);
            } else {
                throw new UnexpectedObjectClassException(
                        String.format(UNEXPECTED_OBJECT_CLASS, "personalResult", personalResult.getClass().getName()));
            }
        }

        private class OkPersonalResultHandler {

            private final String gameId;
            private final String whiteUsername;
            private final String blackUsername;
            private final UserWaitingForGame whiteUwfg;
            private final UserWaitingForGame blackUwfg;
            private String websocketResponseAsJsonString;

            public OkPersonalResultHandler(StartGameOkPersonalResultDto okResult) {
                this.gameId = okResult.getGameId();
                this.whiteUsername = okResult.getWhiteUsername();
                this.blackUsername = okResult.getBlackUsername();
                this.whiteUwfg = uwfgList.get(usernames.indexOf(whiteUsername));
                this.blackUwfg = uwfgList.get(usernames.indexOf(blackUsername));
            }

            private void handleOkPersonalResult() {
                createNewChessGameUserRatingsRecord();
                sendFindGameOkResponseAndFinishSearch();
            }

            private void createNewChessGameUserRatingsRecord() {
                ChessGameUsersRatingsRecord chessGameUsersRatingsRecord = ChessGameUsersRatingsRecord.builder()
                        .gameId(gameId).chessGameType(gameType).startedAt(startedAt)
                        .whiteUsername(whiteUsername).whiteInitialRating(whiteUwfg.getCurrentUserRating().getRating())
                        .blackUsername(blackUsername).blackInitialRating(blackUwfg.getCurrentUserRating().getRating())
                        .build();

                matcherService.saveChessGameUserRatingsRecord(chessGameUsersRatingsRecord);
            }

            private void sendFindGameOkResponseAndFinishSearch() {
                websocketResponseAsJsonString = websocketMessageSender.mapResponseToJsonString(
                        new FindGameOkResponse(gameId));

                sendFindGameOkResponse(whiteUwfg);
                sendFindGameOkResponse(blackUwfg);

                synchronized (uwfgMap) {
                    uwfgMap.remove(whiteUsername);
                    uwfgMap.remove(blackUsername);
                }
            }

            private void sendFindGameOkResponse(UserWaitingForGame uwfg) {
                WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();
                websocketMessageSender.sendMessageToWebsocketSession(websocketResponseAsJsonString,
                        websocketSessionWrapper.getSession());
                WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper);
                setAfterSearchFinishedTimeoutDisconnectTask(websocketSessionWrapper);
            }
        }

        private void handleBadEnemyPersonalResult(StartGameBadEnemyPersonalResultDto badEnemyResult) {
            String username = badEnemyResult.getUsername();
            log.debug(String.format(BAD_ENEMY_RESULT_LOG, username));
            UserWaitingForGame uwfg = uwfgList.get(usernames.indexOf(username));
            uwfg.setWaitingForAnswer(false);
            updateTimeoutDisconnectTaskIfRequired(uwfg, username);
        }

        private void updateTimeoutDisconnectTaskIfRequired(UserWaitingForGame uwfg, String username) {
            WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();

            if (websocketSessionWrapper != null) {
                ScheduledFuture<?> timeoutDisconnectTask = websocketSessionWrapper.getTimeoutDisconnectTask();

                if (timeoutDisconnectTask == null || timeoutDisconnectTask.isDone()) {
                    websocketSessionWrapper.setTimeoutDisconnectTask(
                            scheduledExecutorService.schedule(new TimeoutSearchAbortHandler(username),
                                    MatcherConstants.GAME_SEARCH_TIMEOUT_MS, TimeUnit.MILLISECONDS));
                    log.debug(String.format(DISCONNECT_TASK_UPDATED_LOG, username));
                }
            } else {
                removeUserFromMap(username);
            }
        }

        private void handleAlreadyInGamePersonalResult(StartGameAlreadyInGamePersonalResultDto alreadyInGameResult) {
            String requestingUsername = alreadyInGameResult.getRequestingUsername();
            String gameId = alreadyInGameResult.getGameId();
            ChessGameTypeWithTimings gameType = alreadyInGameResult.getGameType();
            ChessGameType generalGameType = gameType.getGeneralGameType();
            Instant startedAt = alreadyInGameResult.getStartedAt();
            String whiteUsername = alreadyInGameResult.getWhiteUsername();
            CurrentUserRating whiteRating;
            String blackUsername = alreadyInGameResult.getBlackUsername();
            CurrentUserRating blackRating;

            UserWaitingForGame requestingUwfg = uwfgList.get(usernames.indexOf(requestingUsername));

            try {
                whiteRating = matcherService.getCurrentUserRating(whiteUsername, generalGameType);
                blackRating = matcherService.getCurrentUserRating(blackUsername, generalGameType);
            } catch (DatabaseRecordNotFoundException e) {
                log.error(e.toString());
                handleUserSearchAbort(requestingUsername, requestingUwfg, SERVER_ERROR);
                removeUserFromMap(requestingUsername);
                return;
            }

            ChessGameUsersRatingsRecord chessGameUsersRatingsRecord = ChessGameUsersRatingsRecord.builder()
                    .gameId(gameId).chessGameType(gameType).startedAt(startedAt)
                    .whiteUsername(whiteUsername).whiteInitialRating(whiteRating.getRating())
                    .blackUsername(blackUsername).blackInitialRating(blackRating.getRating())
                    .build();

            String errorMessage;
            boolean chessGameUserRatingsRecordSaved;

            saveChessGameUserRatingsRecordLock.lock();
            try {
                chessGameUserRatingsRecordSaved = matcherService.saveChessGameUserRatingsRecordIfNotAlready(
                        chessGameUsersRatingsRecord);
            } finally {
                saveChessGameUserRatingsRecordLock.unlock();
            }

            if (chessGameUserRatingsRecordSaved) {
                errorMessage = String.format(ALREADY_IN_GAME, gameId);
            } else {
                errorMessage = SERVER_ERROR;
            }

            handleUserSearchAbort(errorMessage, requestingUsername, requestingUwfg);
            removeUserFromMap(requestingUsername);
        }

        private void removeUserFromMap(String username) {
            synchronized (uwfgMap) {
                uwfgMap.remove(username);
            }
        }

        private void handleMultipleActiveGamesResult(
                StartGameMultipleActiveGamesPersonalResultDto multipleActiveGamesResult) {
            String username = multipleActiveGamesResult.getUsername();
            log.error(multipleActiveGamesResult.getMessage());
            handleUserSearchAbort(SERVER_ERROR, username, uwfgList.get(usernames.indexOf(username)));
            removeUserFromMap(username);
        }

    }

    @AllArgsConstructor
    private class TimeoutSearchAbortHandler implements Runnable {

        private final String username;

        @Override
        public void run() {
            abortSearchOnTimeoutIfPossible(username);
        }
    }
}
