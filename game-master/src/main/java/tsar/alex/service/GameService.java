package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tsar.alex.api.client.GameMasterRestClient;
import tsar.alex.dto.*;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.request.UpdateRatingsAfterGameRequest;
import tsar.alex.dto.response.StartGameOkResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameBadResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameOkResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameResponse;
import tsar.alex.dto.websocket.request.ChessGameWebsocketRequestEnum;
import tsar.alex.dto.websocket.response.ChessGameWebsocketResponseEnum.ChessGameWebsocketBadResponseEnum;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedDatabaseQueryResultException;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.mapper.GameMasterMapper;
import tsar.alex.model.*;
import tsar.alex.model.chessPieces.King;
import tsar.alex.model.chessPieces.Pawn;
import tsar.alex.model.chessPieces.Rook;
import tsar.alex.repository.GameRepository;
import tsar.alex.utils.ChessFactory;
import tsar.alex.utils.ChessGameConstants;
import tsar.alex.utils.ChessGameUtils;
import tsar.alex.utils.GameMasterUtils;
import tsar.alex.utils.Utils;
import tsar.alex.websocket.ChessGameWebsocketRoom;
import tsar.alex.websocket.ChessGameWebsocketRoomsHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


@Service
@AllArgsConstructor
@Slf4j
public class GameService {

    private final GameMasterRestClient gameMasterRestClient;
    private final Validator validator;
    private final GameMasterMapper mapper;
    private final ThreadLocalRandom threadLocalRandom;
    private final ChessGameWebsocketRoomsHolder chessGameWebsocketRoomsHolder;
    private final GameRepository gameRepository;
    private final UpdateRatingsService updateRatingsService;

    public StartGameOkResponse startGamesIfNotAlready(StartGameRequest startGameRequest) {
        return new StartGamesHandler(startGameRequest).startGamesIfNotAlready();
    }

    private class StartGamesHandler {

        private final ChessGameTypeWithTimings gameType;
        private final List<String> usernames;
        private final int numberOfUsernames;

        private Instant startedAt;
        private ChessPositionsRecord initialChessPositionsRecord;
        private List<List<StartGameAlreadyInGamePersonalResultDto>> activeGamesForAllUsernames;
        private List<StartGamePersonalResultDto> resultList;
        private List<Game> gamesToStart;

        StartGamesHandler(StartGameRequest startGameRequest) {
            this.gameType = startGameRequest.getGameType();
            this.usernames = startGameRequest.getUsernames();
            this.numberOfUsernames = usernames.size();
        }

        private StartGameOkResponse startGamesIfNotAlready() {
            retrieveActiveGamesForAllUsernames();
            log.debug(String.format("startGamesIfNotAlready. Active games = %s", activeGamesForAllUsernames));

            startedAt = Instant.now();
            initialChessPositionsRecord = new ChessPositionsRecord();
            initialChessPositionsRecord.handleNewChessPosition(-1, ChessFactory.INITIAL_BOARD_STATE);

            prepareNewGamesAndNegativePersonalResults();
            log.debug(String.format("startGamesIfNotAlready. Games to start = %s", gamesToStart));

            startGamesAndPrepareOkPersonalResults();

            return new StartGameOkResponse(startedAt, resultList);
        }

        private void retrieveActiveGamesForAllUsernames() {
            List<Game> activeGames = gameRepository.findActiveGamesByUsernames(usernames);
            log.debug(String.format("retrieveActiveGamesForAllUsernames. Active games = %s", activeGames));
            activeGamesForAllUsernames = Stream.generate(
                    () -> (List<StartGameAlreadyInGamePersonalResultDto>) null).limit(usernames.size()).collect(
                    Collectors.toList());

            for (Game game : activeGames) {
                UsersInGame usersInGame = game.getUsersInGame();
                addActiveGameForUsername(game, usersInGame.getWhiteUsername());
                addActiveGameForUsername(game, usersInGame.getBlackUsername());
            }
        }

        private void addActiveGameForUsername(Game game, String username) {
            int usernameIndex = usernames.indexOf(username);

            if (usernameIndex >= 0) {
                List<StartGameAlreadyInGamePersonalResultDto> activeGamesForUsername = activeGamesForAllUsernames.get(
                        usernameIndex);
                if (activeGamesForUsername == null) {
                    activeGamesForUsername = new ArrayList<>(1);
                    activeGamesForAllUsernames.set(usernameIndex, activeGamesForUsername);
                }
                activeGamesForUsername.add(mapper.mapToStartGameAlreadyInGamePersonalResultDto(game, username));
            }
        }

        private void prepareNewGamesAndNegativePersonalResults() {
            resultList = new ArrayList<>(numberOfUsernames / 2);
            gamesToStart = new ArrayList<>(numberOfUsernames / 2);

            for (int firstUserIndex = 0; firstUserIndex < numberOfUsernames; firstUserIndex += 2) {

                int secondUserIndex = firstUserIndex + 1;
                String firstUsername = usernames.get(firstUserIndex);
                String secondUsername = usernames.get(secondUserIndex);
                List<StartGameAlreadyInGamePersonalResultDto> firstUserActiveGames = activeGamesForAllUsernames.get(
                        firstUserIndex);
                List<StartGameAlreadyInGamePersonalResultDto> secondUserActiveGames = activeGamesForAllUsernames.get(
                        secondUserIndex);

                if (firstUserActiveGames == null && secondUserActiveGames == null) {
                    gamesToStart.add(prepareNewGame(firstUsername, secondUsername));
                } else {
                    resultList.add(prepareStartGameNegativePersonalResult(firstUserActiveGames, firstUsername));
                    resultList.add(prepareStartGameNegativePersonalResult(secondUserActiveGames, secondUsername));
                }
            }
        }

        private Game prepareNewGame(String username1, String username2) {
            UsersInGame usersInGame = threadLocalRandom.nextBoolean() ? new UsersInGame(username1, username2)
                    : new UsersInGame(username2, username1);

            return Game.builder()
                    .gameType(gameType)
                    .startedAt(startedAt)
                    .usersInGame(usersInGame)
                    .boardState(ChessFactory.INITIAL_BOARD_STATE)
                    .chessPositionsRecord(initialChessPositionsRecord)
                    .build();
        }

        private StartGamePersonalResultDto prepareStartGameNegativePersonalResult(
                List<StartGameAlreadyInGamePersonalResultDto> userActiveGames, String username) {

            if (userActiveGames == null) return new StartGameBadEnemyPersonalResultDto(username);
            if (userActiveGames.size() == 1) return userActiveGames.get(0);

            String[] gamesIds = userActiveGames.stream().map(StartGameAlreadyInGamePersonalResultDto::getGameId)
                    .toArray(String[]::new);
            return new StartGameMultipleActiveGamesPersonalResultDto(username,
                    String.format(MULTIPLE_ACTIVE_GAMES, username, Arrays.toString(gamesIds)));
        }

        private void startGamesAndPrepareOkPersonalResults() {
            List<Game> savedGames = gameRepository.saveAll(gamesToStart);

            for (Game game : savedGames) {
                ChessGameWebsocketRoom gameWebsocketRoom = chessGameWebsocketRoomsHolder.addGameWebsocketRoom(
                        game.getId(), gameType, game.getUsersInGame());

                gameWebsocketRoom.reentrantLock.lock();
                try {
                    gameWebsocketRoom.setTimeoutDisconnectTask(ChessColor.WHITE, TimeoutTypeEnum.TIME_IS_UP,
                            ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS);
                } finally {
                    gameWebsocketRoom.reentrantLock.unlock();
                }

                resultList.add(mapper.mapToStartGameOkPersonalResultDto(game));
            }
        }
    }


    public GameStateResponse getGameState(String gameId) {
        Optional<Game> GameOptional = gameRepository.findById(gameId);

        if (GameOptional.isEmpty()) {
            return new GameStateBadResponse(String.format(NO_GAME, gameId));
        }

        Game game = GameOptional.get();

        return mapper.mapToGameStateOkResponse(game);
    }

    public void makeMove(String gameId, ChessMove chessMove) {

        ChessGameWebsocketRoom gameWebsocketRoom = chessGameWebsocketRoomsHolder.getGameWebsocketRoom(gameId);

        if (gameWebsocketRoom == null) {
            throw new WebsocketException(String.format(NO_ACTIVE_GAME, gameId),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_GAME);
        }

        gameWebsocketRoom.reentrantLock.lock();

        try {
            gameWebsocketRoom.checkFinished();
            String username = GameMasterUtils.getCurrentUsername();
            gameWebsocketRoom.checkSubscribed(username);

            Game game = gameRepository.findById(gameId).orElseThrow(
                    () -> new DatabaseRecordNotFoundException(String.format(DB_AND_RAM_OBJECT_NOT_CORRESPOND_ID, gameId)));

            if (game.isFinished()) {
                throw new UnexpectedDatabaseQueryResultException(
                        String.format(DB_AND_RAM_OBJECT_NOT_CORRESPOND_FINISHED, gameId));
            }

            if (chessMove == null) {
                gameWebsocketRoom.sendBadResponse(ChessGameWebsocketBadResponseEnum.CHESS_MOVE_BAD, username,
                        CHESS_MOVE_NULL);
                return;
            }

            Set<ConstraintViolation<Object>> violations = validator.validate(chessMove);
            if (!violations.isEmpty()) {
                gameWebsocketRoom.sendBadResponse(ChessGameWebsocketBadResponseEnum.CHESS_MOVE_BAD, username,
                        Utils.getConstraintViolationsAsString(violations));
                return;
            }

            UsersInGame usersInGame = game.getUsersInGame();
            int currentMoveNumber = game.getCurrentMoveNumber();

            if (!username.equals(ChessGameUtils.getCurrentTurnUsername(usersInGame, currentMoveNumber))) {
                gameWebsocketRoom.sendBadResponse(ChessGameWebsocketBadResponseEnum.CHESS_MOVE_BAD, username,
                        NOT_YOUR_TURN);
                return;
            }

            ChessColor currentTurnUserColor = ChessGameUtils.getUserColorByMoveNumber(currentMoveNumber);
            ChessPiece[][] boardState = game.getBoardState();
            ChessCoords startCoords = chessMove.getStartCoords();

            if (!ChessGameUtils.validateChessMove(chessMove, startCoords, currentTurnUserColor, boardState)) {
                gameWebsocketRoom.sendBadResponse(ChessGameWebsocketBadResponseEnum.CHESS_MOVE_BAD, username, BAD_MOVE);
                return;
            }

            ChessPiece startPiece = boardState[startCoords.getNumberCoord()][startCoords.getLetterCoord()];

            if (!startPiece.makeMoveIfPossible(game, chessMove)) {
                gameWebsocketRoom.sendBadResponse(ChessGameWebsocketBadResponseEnum.CHESS_MOVE_BAD, username, BAD_MOVE);
                return;
            }
            chessMove.setMoveNumber(currentMoveNumber);

            checkIfChessPositionIsChangedIrreversibly(game, chessMove);

            ChessColor enemyColor = ChessColor.getInvertedColor(currentTurnUserColor);

            boolean finished = checkIfGameIsFinished(game, chessMove, currentMoveNumber, currentTurnUserColor,
                    enemyColor);

            updateTimeLeftAndLastMoveTime(game, finished, chessMove, currentMoveNumber, currentTurnUserColor,
                    enemyColor);

            game.getChessMovesRecord().add(chessMove);
            game.setFinished(finished);

            gameWebsocketRoom.makeMoveOkResponse(chessMove);

            gameRepository.save(game);

            if (finished) {
                gameWebsocketRoom.finishGameWebsockets(game.getResult());
                updateRatingsService.updateRatingsAfterGameFinished(List.of(game));
            }

        } finally {
            gameWebsocketRoom.reentrantLock.unlock();
        }

    }


    private void checkIfChessPositionIsChangedIrreversibly(Game game, ChessMove chessMove) {
        if (chessMove.getEndPiece() != null || chessMove.getStartPiece() == ChessPieceEnum.PAWN
                && chessMove.getStartCoords().getLetterCoord() != chessMove.getEndCoords().getLetterCoord()) {
            game.getChessPositionsRecord().clearChessPositionsRecord(true, chessMove.getMoveNumber());
        } else if (chessMove.getStartPiece() == ChessPieceEnum.PAWN || hasCastlingRightBeenLost(game.getBoardState(),
                chessMove) || hasEnPassantRightBeenLost(game.getBoardState(), chessMove)) {
            game.getChessPositionsRecord().clearChessPositionsRecord(false, chessMove.getMoveNumber());
        }
    }

    private boolean hasCastlingRightBeenLost(ChessPiece[][] boardState, ChessMove chessMove) {
        ChessCoords startCoords = chessMove.getStartCoords();

        if (chessMove.getStartPiece() == ChessPieceEnum.KING && chessMove.isStartPieceFirstMove() &&
                (boardState[startCoords.getNumberCoord()][0] instanceof Rook rook1 && rook1.isFirstMove() ||
                        boardState[startCoords.getNumberCoord()][7] instanceof Rook rook2 && rook2.isFirstMove())) {
            return true;
        }

        if (chessMove.getStartPiece() == ChessPieceEnum.ROOK && chessMove.isStartPieceFirstMove() &&
                boardState[startCoords.getNumberCoord()][4] instanceof King king && king.isFirstMove()) {
            return true;
        }

        return false;
    }

    private boolean hasEnPassantRightBeenLost(ChessPiece[][] boardState, ChessMove chessMove) {
        ChessCoords enPassantCoords = chessMove.getPreviousEnPassantCoords();

        if (enPassantCoords == null) {
            return false;
        }

        int numberCoord = enPassantCoords.getNumberCoord();
        int letterCoord = enPassantCoords.getLetterCoord();

        int minNeighbourPieceLetterCoord = Math.max(letterCoord - 1, 0);
        int maxNeighbourPieceLetterCoord = Math.min(letterCoord + 1, 7);

        ChessColor enPassantPawnColor = boardState[numberCoord][letterCoord].getColor();

        for (int j = minNeighbourPieceLetterCoord; j <= maxNeighbourPieceLetterCoord; j += 2) {
            if (boardState[numberCoord][j] instanceof Pawn pawn && pawn.getColor() != enPassantPawnColor) {
                return true;
            }
        }

        return false;
    }

    private void updateTimeLeftAndLastMoveTime(Game game, boolean finished, ChessMove chessMove, int currentMoveNumber,
            ChessColor currentTurnUserColor, ChessColor enemyColor) {
        TimeLefts timelefts = ChessGameUtils.calculateTimeLefts(game, finished, currentMoveNumber, currentTurnUserColor,
                enemyColor);
        game.setLastMoveTimeMS(timelefts.getThisMoveTime());
        long currentUserTimeLeft = timelefts.getNewTimeLeft();
        long enemyTimeLeft = timelefts.getEnemyTimeLeft();
        chessMove.setTimeLeftByUserColor(currentTurnUserColor, currentUserTimeLeft);
        chessMove.setTimeLeftByUserColor(enemyColor, enemyTimeLeft);

        if (finished) {
            ChessGameResult GameResult = game.getResult();
            GameResult.setTimeLeftByUserColor(currentTurnUserColor, currentUserTimeLeft);
            GameResult.setTimeLeftByUserColor(enemyColor, enemyTimeLeft);
        }
    }

    private boolean checkIfGameIsFinished(Game game, ChessMove chessMove, int currentMoveNumber,
            ChessColor currentTurnUserColor, ChessColor enemyColor) {
        ChessPiece[][] boardState = game.getBoardState();

        ChessCoords enemyKingCoords = ChessPiece.findKingCoords(boardState, enemyColor);
        boolean check = ChessPiece.isUnderAttack(boardState, enemyKingCoords, enemyColor);

        boolean enemyHavePossibleMoves = ChessPiece.doesEnemyHavePossibleMoves(game, enemyKingCoords, enemyColor);

        if (!enemyHavePossibleMoves) {
            ChessGameResult gameResult = new ChessGameResult();

            if (check) {
                gameResult.setWinnerColor(currentTurnUserColor);
                gameResult.setMessage("Мат");
                chessMove.setResult(ChessMoveResult.MATE);
            } else {
                gameResult.setDraw(true);
                gameResult.setMessage("Пат");
            }

            game.setResult(gameResult);
            game.setFinished(true);
            return true;
        }

        ChessPositionStatusEnum positionStatus = game.getChessPositionsRecord()
                .handleNewChessPosition(currentMoveNumber, boardState);

        if (positionStatus != ChessPositionStatusEnum.OK) {
            ChessGameResult gameResult = new ChessGameResult();
            gameResult.setDraw(true);
            gameResult.setMessage(positionStatus.getMessage());
            game.setResult(gameResult);
            game.setFinished(true);
            return true;
        }

        if (check) {
            chessMove.setResult(ChessMoveResult.CHECK);
        }

        return false;
    }


    public void handleUserGameRequest(String gameId, ChessGameWebsocketRequestEnum requestType) {
        ChessGameWebsocketRoom gameWebsocketRoom = chessGameWebsocketRoomsHolder.getGameWebsocketRoom(gameId);

        if (gameWebsocketRoom == null) {
            throw new WebsocketException(String.format(NO_ACTIVE_GAME, gameId),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_GAME);
        }

        gameWebsocketRoom.reentrantLock.lock();

        try {
            gameWebsocketRoom.checkFinished();
            String username = GameMasterUtils.getCurrentUsername();
            gameWebsocketRoom.checkSubscribed(username);
            switch (requestType) {
                case INFO:
                    gameWebsocketRoom.sendInfo(username);
                    break;
                case DRAW:
                    gameWebsocketRoom.makeDrawOffer(username);
                    break;
                case ACCEPT_DRAW:
                    gameWebsocketRoom.acceptDrawOffer(username);
                    break;
                case REJECT_DRAW:
                    gameWebsocketRoom.rejectDrawOffer(username);
                    break;
                case SURRENDER:
                    gameWebsocketRoom.surrender(username);
                    break;
                default:
                    throw new RuntimeException("Incorrect requestType: " + requestType);
            }
        } finally {
            gameWebsocketRoom.reentrantLock.unlock();
        }
    }

}
