package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tsar.alex.dto.*;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;
import tsar.alex.dto.response.StartGameBadResponse;
import tsar.alex.dto.response.StartGameOkResponse;
import tsar.alex.dto.response.StartGameResponse;
import tsar.alex.dto.websocket.request.ChessGameWebsocketRequestEnum;
import tsar.alex.dto.websocket.response.ChessGameWebsocketResponseEnum.ChessGameWebsocketBadResponseEnum;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedDatabaseQueryResultException;
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
import tsar.alex.api.websocket.ChessGameWebsocketRoom;
import tsar.alex.api.websocket.ChessGameWebsocketRoomsHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class GameService {

    private final Validator validator;

    private final GameMasterMapper mapper;
    private final ThreadLocalRandom threadLocalRandom;

    private final ChessGameWebsocketRoomsHolder chessGameWebsocketRoomsHolder;

    private final GameRepository gameRepository;


    public StartGameResponse startGame(StartGameRequest startGameRequest) {

        ChessGameTypeWithTimings gameType = startGameRequest.getChessGameTypeWithTimings();
        Pair<String> usernames = startGameRequest.getPairOfUsernames();
        String errorMessage = checkUsersToStartGame(usernames);

        if (errorMessage.length() > 0) {
            return new StartGameBadResponse(errorMessage);
        }

        boolean sameUsersOrder;
        UsersInGame usersInGame;

        if (threadLocalRandom.nextBoolean()) {
            usersInGame = new UsersInGame(usernames.get(0), usernames.get(1));
            sameUsersOrder = true;
        } else {
            usersInGame = new UsersInGame(usernames.get(1), usernames.get(0));
            sameUsersOrder = false;
        }

        ChessPiece[][] initialBoardState = ChessFactory.getInitialBoardState();

        ChessPositionsRecord initialChessPositionsRecord = new ChessPositionsRecord();
        initialChessPositionsRecord.handleNewChessPosition(-1, initialBoardState);

        Game game = Game.builder()
                .gameType(gameType)
                .startedAt(Instant.now())
                .usersInGame(usersInGame)
                .boardState(initialBoardState)
                .chessPositionsRecord(initialChessPositionsRecord)
                .build();

        String gameId = gameRepository.save(game).getId();
        ChessGameWebsocketRoom gameWebsocketRoom = chessGameWebsocketRoomsHolder.addGameWebsocketRoom(gameId, gameType,
                usersInGame);
        gameWebsocketRoom.reentrantLock.lock();

        try {
            gameWebsocketRoom.setTimeoutDisconnectTask(ChessColor.WHITE, TimeoutTypeEnum.TIME_IS_UP,
                    ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS);
        } finally {
            gameWebsocketRoom.reentrantLock.unlock();
        }
        return new StartGameOkResponse(gameId, game.getStartedAt(), sameUsersOrder);
    }

    private String checkUsersToStartGame(Pair<String> usernames) {
        String errorMessage = "";

        for (int i = 0; i < 2; i++) {
            String username = usernames.get(i);
            List<Game> activeGamesForSpecificUser = gameRepository.findActiveGamesByUsername(username);
            int numberOfGames = activeGamesForSpecificUser.size();

            if (numberOfGames != 0) {

                if (errorMessage.length() > 0) {
                    errorMessage += ". ";
                }

                errorMessage += "User " + username + " is already in ";

                if (numberOfGames == 1) {
                    errorMessage += "Game with id = " + activeGamesForSpecificUser.get(0).getId();
                } else {
                    List<String> gameIds = activeGamesForSpecificUser.stream().map(Game::getId).toList();
                    errorMessage += "Games with ids = [" + gameIds.stream().map(String::valueOf)
                            .collect(Collectors.joining(", ")) + "]";
                }
            }
        }

        return errorMessage;
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
                    () -> new DatabaseRecordNotFoundException(String.format(DB_AND_OBJECT_NOT_CORRESPOND_ID, gameId)));

            if (game.isFinished()) {
                throw new UnexpectedDatabaseQueryResultException(
                        String.format(DB_AND_OBJECT_NOT_CORRESPOND_FINISHED, gameId));
            }

            if (chessMove == null) {
                gameWebsocketRoom.sendBadResponse(ChessGameWebsocketBadResponseEnum.CHESS_MOVE_BAD, username,
                        CHESS_MOVE_NULL);
                return;
            }

            Set<ConstraintViolation<ChessMove>> violations = validator.validate(chessMove);
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

            gameRepository.save(game);

            gameWebsocketRoom.makeMoveOkResponse(chessMove);

            if (finished) {
                gameWebsocketRoom.finishGameWebsockets(game.getResult());
                UpdateUsersRatingsRequest updateUsersRatingsRequest = mapper.mapToUpdateUsersRatingsRequest(game);
                GameMasterUtils.sendUpdateUsersRatingsRequest(updateUsersRatingsRequest);
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
