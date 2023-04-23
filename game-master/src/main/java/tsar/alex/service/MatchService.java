package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tsar.alex.dto.*;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;
import tsar.alex.dto.response.StartMatchBadResponse;
import tsar.alex.dto.response.StartMatchOkResponse;
import tsar.alex.dto.response.StartMatchResponse;
import tsar.alex.dto.websocket.request.ChessMatchWebsocketRequestEnum;
import tsar.alex.dto.websocket.response.ChessMatchWebsocketResponseEnum.ChessMatchWebsocketBadResponseEnum;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.mapper.GameMasterMapper;
import tsar.alex.model.*;
import tsar.alex.repository.MatchRepository;
import tsar.alex.utils.ChessFactory;
import tsar.alex.utils.ChessGameConstants;
import tsar.alex.utils.ChessGameUtils;
import tsar.alex.utils.GameMasterUtils;
import tsar.alex.utils.Utils;
import tsar.alex.utils.websocket.ChessMatchWebsocketRoom;
import tsar.alex.utils.websocket.ChessMatchWebsocketRoomsHolder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class MatchService {

    private final Validator validator;

    private final GameMasterMapper mapper;
    private final ThreadLocalRandom threadLocalRandom;

    private final ChessMatchWebsocketRoomsHolder chessMatchWebsocketRoomsHolder;

    private final MatchRepository matchRepository;


    public StartMatchResponse startMatch(Pair<String> usernames) {

        String errorMessage = checkUsersToStartMatch(usernames);

        if (errorMessage.length() > 0) {
            return new StartMatchBadResponse(errorMessage);
        }

        boolean sameUsersOrder;
        UsersInMatch usersInMatch;

        if (threadLocalRandom.nextBoolean()) {
            usersInMatch = new UsersInMatch(usernames.get(0), usernames.get(1));
            sameUsersOrder = true;
        } else {
            usersInMatch = new UsersInMatch(usernames.get(1), usernames.get(0));
            sameUsersOrder = false;
        }

        ChessPiece[][] initialBoardState = ChessFactory.getInitialBoardState();

        ChessPositionsRecord initialChessPositionsRecord = new ChessPositionsRecord();
        initialChessPositionsRecord.handleNewChessPosition(-1, initialBoardState);

        Match match = Match.builder()
                .startedAt(Instant.now())
                .usersInMatch(usersInMatch)
                .boardState(initialBoardState)
                .chessMovesRecord(new ArrayList<>())
                .chessPositionsRecord(initialChessPositionsRecord)
                .build();

        String matchId = matchRepository.save(match).getId();
        ChessMatchWebsocketRoom matchWebsocketRoom = chessMatchWebsocketRoomsHolder.addMatchWebsocketRoom(matchId,
                                                                                                usersInMatch);
        matchWebsocketRoom.reentrantLock.lock();

        try {
            matchWebsocketRoom.setTimeoutFinisher(ChessColor.WHITE, TimeoutTypeEnum.TIME_IS_UP,
                    ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS);
        } finally {
            matchWebsocketRoom.reentrantLock.unlock();
        }
        return new StartMatchOkResponse(matchId, match.getStartedAt(), sameUsersOrder);
    }

    private String checkUsersToStartMatch(Pair<String> usernames) {
        String errorMessage = "";

        for (int i = 0; i < 2; i++) {
            String username = usernames.get(i);
            List<Match> activeMatchesForSpecificUser = matchRepository.findActiveMatchesByUsername(username);
            int numberOfMatches = activeMatchesForSpecificUser.size();

            if (numberOfMatches != 0) {

                if (errorMessage.length() > 0) {
                    errorMessage += ". ";
                }

                errorMessage += "User " + username + " is already in ";

                if (numberOfMatches == 1) {
                    errorMessage += "match with id = " + activeMatchesForSpecificUser.get(0).getId();
                } else {
                    List<String> matchIds = activeMatchesForSpecificUser.stream().map(Match::getId).toList();
                    errorMessage += "matches with ids = [" + matchIds.stream().map(String::valueOf)
                            .collect(Collectors.joining(", ")) +"]";
                }
            }
        }

        return errorMessage;
    }


    public MatchStateResponse getMatchState(String matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isEmpty()) {
            return new MatchStateBadResponse(String.format(NO_MATCH, matchId));
        }

        Match match = matchOptional.get();

        return mapper.mapToMatchStateOkResponse(match);
    }

    public void makeMove(String matchId, ChessMove chessMove) {

        ChessMatchWebsocketRoom matchWebsocketRoom = chessMatchWebsocketRoomsHolder.getMatchWebsocketRoom(matchId);

        if (matchWebsocketRoom == null) {
            throw new WebsocketException(String.format(NO_ACTIVE_MATCH, matchId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_MATCH);
        }

        matchWebsocketRoom.reentrantLock.lock();

        try {
            matchWebsocketRoom.checkFinished();
            String username = GameMasterUtils.getCurrentUsername();
            matchWebsocketRoom.checkSubscribed(username);

            Match match = matchRepository.findById(matchId).orElseThrow(()
                    -> new RuntimeException("match DB doesn't contain match with id = " + matchId
                    + ". But chessMatchWebsocketRoomsHolder does"));

            if (match.isFinished()) {
                throw new RuntimeException("Match with id = " + matchId + " is already finished.");
            }

            if (chessMove == null) {
                matchWebsocketRoom.sendBadResponse(ChessMatchWebsocketBadResponseEnum.CHESS_MOVE_BAD,
                        username, "chessMove in null");
                return;
            }
            Set<ConstraintViolation<ChessMove>> violations = validator.validate(chessMove);
            if (!violations.isEmpty()) {
                matchWebsocketRoom.sendBadResponse(ChessMatchWebsocketBadResponseEnum.CHESS_MOVE_BAD,
                        username, Utils.getConstraintViolationsAsString(violations));
                return;
            }

            UsersInMatch usersInMatch = match.getUsersInMatch();
            int currentMoveNumber = match.getCurrentMoveNumber();

            if (!username.equals(ChessGameUtils.getCurrentTurnUsername(usersInMatch, currentMoveNumber))) {
                matchWebsocketRoom.sendBadResponse(ChessMatchWebsocketBadResponseEnum.CHESS_MOVE_BAD,
                                                    username, "It's not your turn");
                return;
            }

            ChessColor currentTurnUserColor = ChessGameUtils.getUserColorByMoveNumber(currentMoveNumber);
            ChessPiece[][] boardState = match.getBoardState();
            ChessCoords startCoords = chessMove.getStartCoords();

            if (!ChessGameUtils.validateChessMove(chessMove, startCoords, currentTurnUserColor, boardState)) {
                System.out.println("Move validation failed");
                matchWebsocketRoom.sendBadResponse(ChessMatchWebsocketBadResponseEnum.CHESS_MOVE_BAD,
                                                    username, "Bad move");
                return;
            }

            ChessPiece startPiece = boardState[startCoords.getNumberCoord()][startCoords.getLetterCoord()];

            if (!startPiece.makeMoveIfPossible(match, chessMove)) {
                matchWebsocketRoom.sendBadResponse(ChessMatchWebsocketBadResponseEnum.CHESS_MOVE_BAD,
                                                    username, "Bad move");
                return;
            }
            chessMove.setMoveNumber(currentMoveNumber);

            checkIfChessPositionIsChangedIrreversibly(match, chessMove);

            ChessColor enemyColor = ChessColor.getInvertedColor(currentTurnUserColor);
            updateTimeLeftAndLastMoveTime(match, chessMove, currentMoveNumber, currentTurnUserColor, enemyColor);

            boolean finished = checkIfMatchIsFinished(match, chessMove, currentMoveNumber, currentTurnUserColor,
                                                        enemyColor);
            match.getChessMovesRecord().add(chessMove);
            match.setFinished(finished);

            matchRepository.save(match);

            matchWebsocketRoom.makeMoveOkResponse(chessMove);


            if (finished) {
                matchWebsocketRoom.finishMatchWebsockets(match.getResult());
                UpdateUsersRatingsRequest updateUsersRatingsRequest = mapper.mapToUpdateUsersRatingsRequest(match);
                GameMasterUtils.sendUpdateUsersRatingsRequest(updateUsersRatingsRequest);
            }

        } finally {
            matchWebsocketRoom.reentrantLock.unlock();
        }

    }

    private void checkIfChessPositionIsChangedIrreversibly(Match match, ChessMove chessMove) {
        if (chessMove.getEndPiece() != null || chessMove.getStartPiece().getName().equals("pawn")
                || chessMove.isStartPieceFirstMove() || chessMove.getPreviousEnPassantCoords() != null) {
            match.setChessPositionsRecord(new ChessPositionsRecord(chessMove.getMoveNumber(), new HashMap<>()));
        }
    }

    private void updateTimeLeftAndLastMoveTime(Match match, ChessMove chessMove, int currentMoveNumber,
                                               ChessColor currentTurnUserColor, ChessColor enemyColor) {
        TimeLefts timelefts = ChessGameUtils.calculateTimeLefts(match, currentMoveNumber, currentTurnUserColor,
                enemyColor);
        match.setLastMoveTimeMS(timelefts.getThisMoveTime());
        chessMove.setTimeLeftByUserColor(currentTurnUserColor, timelefts.getNewTimeLeft());
        chessMove.setTimeLeftByUserColor(enemyColor, timelefts.getEnemyTimeLeft());
    }

    private boolean checkIfMatchIsFinished(Match match, ChessMove chessMove, int currentMoveNumber,
                                           ChessColor currentTurnUserColor, ChessColor enemyColor) {
        ChessPiece[][] boardState = match.getBoardState();

        ChessCoords enemyKingCoords = ChessPiece.findKingCoords(boardState, enemyColor);
        boolean check = ChessPiece.isUnderAttack(boardState, enemyKingCoords, enemyColor);

        boolean enemyHavePossibleMoves = ChessPiece.doesEnemyHavePossibleMoves(match, enemyKingCoords, enemyColor);

        if (!enemyHavePossibleMoves) {
            ChessMatchResult matchResult = new ChessMatchResult();
            matchResult.setWhiteTimeLeftMS(chessMove.getWhiteTimeLeftMS());
            matchResult.setBlackTimeLeftMS(chessMove.getBlackTimeLeftMS());

            if (check) {
                matchResult.setWinnerColor(currentTurnUserColor);
                matchResult.setMessage("Мат");
                chessMove.setResult(ChessMoveResult.MATE);
            } else {
                matchResult.setDraw(true);
                matchResult.setMessage("Пат");
            }

            match.setResult(matchResult);
            match.setFinished(true);
            return true;
        }

        ChessPositionCheckResultEnum positionCheckResult = match.getChessPositionsRecord()
                                                            .handleNewChessPosition(currentMoveNumber, boardState);
        if (positionCheckResult != ChessPositionCheckResultEnum.OK) {
            ChessMatchResult matchResult = new ChessMatchResult();
            matchResult.setWhiteTimeLeftMS(chessMove.getWhiteTimeLeftMS());
            matchResult.setBlackTimeLeftMS(chessMove.getBlackTimeLeftMS());
            matchResult.setDraw(true);

            String message;

            switch (positionCheckResult) {
                case THREEFOLD_REPETITION:
                    message = "Трёхкратное повторение позиции";
                    break;
                case FIFTY_MOVE_NOT_CHANGE:
                    message = "50 ходов без необратимых изменений в положении фигур";
                    break;
                default:
                    throw new RuntimeException("Incorrect ChessPositionCheckResultEnum object");
            }

            matchResult.setMessage(message);
            match.setResult(matchResult);
            match.setFinished(true);
            return true;
        }

        if (check) {
            chessMove.setResult(ChessMoveResult.CHECK);
        }

        return false;
    }


    public void handleUserMatchRequest(String matchId, ChessMatchWebsocketRequestEnum requestType) {
        ChessMatchWebsocketRoom matchWebsocketRoom = chessMatchWebsocketRoomsHolder.getMatchWebsocketRoom(matchId);

        if (matchWebsocketRoom == null) {
            throw new WebsocketException(String.format(NO_ACTIVE_MATCH, matchId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_MATCH);
        }

        matchWebsocketRoom.reentrantLock.lock();

        try {
            matchWebsocketRoom.checkFinished();
            String username = GameMasterUtils.getCurrentUsername();
            matchWebsocketRoom.checkSubscribed(username);
            switch (requestType) {
                case INFO:
                    matchWebsocketRoom.sendInfo(username);
                    break;
                case DRAW:
                    matchWebsocketRoom.makeDrawOffer(username);
                    break;
                case ACCEPT_DRAW:
                    matchWebsocketRoom.acceptDrawOffer(username);
                    break;
                case REJECT_DRAW:
                    matchWebsocketRoom.rejectDrawOffer(username);
                    break;
                case SURRENDER:
                    matchWebsocketRoom.surrender(username);
                    break;
                default:
                    throw new RuntimeException("Incorrect request type: " + requestType);
            }
        } finally {
            matchWebsocketRoom.reentrantLock.unlock();
        }
    }

}
