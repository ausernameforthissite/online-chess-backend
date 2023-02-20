package tsar.alex.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tsar.alex.dto.*;
import tsar.alex.model.*;
import tsar.alex.repository.MatchRecordRepository;
import tsar.alex.repository.MatchRepository;
import tsar.alex.utils.ChessGameUtils;
import tsar.alex.utils.Utils;
import tsar.alex.utils.sse.ChessMoveSseEmitters;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;


@Service
@AllArgsConstructor
public class MatchService {


    private final Map<Long, ChessMoveSseEmitters> emitters;
    private final ExecutorService threadPool;

    private final MatchRepository matchRepository;
    private final MatchRecordRepository matchRecordRepository;


    public MatchStateResponse getMatchState(long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isEmpty()) {
            return new MatchStateBadResponse("Match with id = " + matchId + " was not found");
        }

        Match match = matchOptional.get();

        MatchRecord matchRecord = matchRecordRepository.findById(matchId).orElseThrow(()
                -> new RuntimeException("match_record DB doesn't contain match with id = " + matchId));

        return new MatchStateOkResponse(match, matchRecord.getChessMovesRecord());
    }


    public MakeMoveResponse makeMove(long matchId, ChessMove chessMove) {

        if (!emitters.containsKey(matchId)) {
            return new MakeMoveBadResponse("No active match with id = " + matchId + " was found");
        }

        Match match = matchRepository.findById(matchId).orElseThrow(() -> new RuntimeException("match DB doesn't contain match with id = " + matchId + ". But emitters map does"));

        if (match.isFinished()) {
            return new MakeMoveBadResponse("Match with id = " + matchId + " is already finished.");
        }

        String username = Utils.getCurrentUsername();
        UsersInMatch usersInMatch = match.getUsersInMatch();

        if (!username.equals(usersInMatch.getCurrentTurnUsername())) {
            return new MakeMoveBadResponse("It's not your turn");
        }

        ChessColor userColor = usersInMatch.getCurrentTurnUserColor();
        ChessPiece[][] boardState = match.getBoardState();
        ChessCoords startCoords = chessMove.getStartCoords();

        if (ChessGameUtils.validateChessMove(chessMove, startCoords, userColor, boardState)) {
            return new MakeMoveBadResponse("Bad move");
        }

        ChessPiece startPiece = boardState[startCoords.getNumberCoord()][startCoords.getLetterCoord()];

        if (!startPiece.makeMoveIfPossible(match, chessMove)) {
            return new MakeMoveBadResponse("Bad move");
        }

        boolean finished = checkIfMatchIsFinished(match, chessMove);


        MatchRecord matchRecord = matchRecordRepository.findById(matchId).orElseThrow(()
                -> new RuntimeException("match_record DB doesn't contain record with match_id = " + matchId));

        matchRecord.getChessMovesRecord().add(chessMove);
        match.setFinished(finished);

        matchRepository.save(match);
        matchRecordRepository.save(matchRecord);

        sendChessMove(matchId, chessMove, finished);
        return new MakeMoveOKResponse();
    }


    private boolean checkIfMatchIsFinished(Match match, ChessMove chessMove) {
        UsersInMatch usersInMatch = match.getUsersInMatch();
        ChessColor enemyColor = ChessColor.getInvertedColor(usersInMatch.getCurrentTurnUserColor());
        ChessPiece[][] boardState = match.getBoardState();

        ChessCoords enemyKingCoords = ChessPiece.findKingCoords(boardState, enemyColor);
        boolean check = ChessPiece.isUnderAttack(boardState, enemyKingCoords, enemyColor);

        boolean enemyHavePossibleMoves = ChessPiece.doesEnemyHavePossibleMoves(match, enemyKingCoords, enemyColor);

        if (enemyHavePossibleMoves) {
            if (check) {
                chessMove.setChessMoveResult(ChessMoveResult.CHECK);
            }
            usersInMatch.setCurrentTurnUserColor(enemyColor);
            return false;
        } else {
            if (check) {
                chessMove.setChessMoveResult(ChessMoveResult.MATE);
            }
            match.setFinished(true);
            return true;
        }
    }

    private void sendChessMove(long matchId, ChessMove chessMove, boolean finished) {
        threadPool.execute(() -> {
            ChessMoveSseEmitters matchSubscribers = emitters.get(matchId);
            if (matchSubscribers != null) {
                matchSubscribers.sendToAllSubscribers(chessMove);
                if (finished) {
                    matchSubscribers.completeAll();
                    emitters.remove(matchId);
                }
            }
        });
    }

}
