package tsar.alex.utils;

import tsar.alex.dto.TimeLefts;
import tsar.alex.model.*;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;

public class ChessGameUtils {

    public static boolean isCoordPossible(int coord) {
        return coord >= 0 && coord < BOARD_LENGTH;
    }

    public static boolean areCoordsPossible(ChessCoords coords) {
        return isCoordPossible(coords.getNumberCoord()) && isCoordPossible(coords.getLetterCoord());
    }

    public static boolean validateChessMove(ChessMove chessMove, ChessCoords startCoords,
                                            ChessColor userColor, ChessPiece[][] boardState) {
        ChessCoords endCoords = chessMove.getEndCoords();

        if (startCoords.equals(endCoords)) {
            return false;
        }

        if (!(ChessGameUtils.areCoordsPossible(startCoords) && ChessGameUtils.areCoordsPossible(endCoords))) {
            return false;
        }

        ChessPiece startPiece = boardState[startCoords.getNumberCoord()][startCoords.getLetterCoord()];

        if (startPiece == null || startPiece.getColor() != userColor) {
            return false;
        }

        if (!startPiece.getClass().getSimpleName().equalsIgnoreCase(chessMove.getStartPiece().getName())) {
            return false;
        }
        if (chessMove.getCastling() != 0) {
            if (chessMove.getStartPiece() != ChessPieceEnum.KING || chessMove.getEndPiece() != null) {
                return false;
            }

            if (Math.abs(chessMove.getCastling()) > 1 ) {
                return false;
            }

            if (startCoords.getNumberCoord() != endCoords.getNumberCoord()) {
                return false;
            }
        }

        if (chessMove.getPawnPromotionPiece() != null) {
            if (chessMove.getStartPiece() != ChessPieceEnum.PAWN){
                return false;
            }

            if (chessMove.getPawnPromotionPiece() == ChessPieceEnum.PAWN || chessMove.getPawnPromotionPiece() == ChessPieceEnum.KING) {
                return false;
            }
        }

        ChessPiece endPiece = boardState[endCoords.getNumberCoord()][endCoords.getLetterCoord()];

        if (endPiece == null) {
            if (chessMove.getEndPiece() != null) {
                return false;
            }
        } else {
            if (chessMove.getEndPiece() == null) {
                return false;
            }

            if (!endPiece.getClass().getSimpleName().equalsIgnoreCase(chessMove.getEndPiece().getName())) {
                return false;
            }

            if (userColor == endPiece.getColor()) {
                return false;
            }
        }

        return true;
    }

    public static String getCurrentTurnUsername(UsersInMatch usersInMatch, int currentMoveNumber) {
        if (currentMoveNumber % 2 == 0) {
            return usersInMatch.getWhiteUsername();
        } else {
            return usersInMatch.getBlackUsername();
        }
    }

    public static ChessColor getUserColorByMoveNumber(int currentMoveNumber) {
        if (currentMoveNumber % 2 == 0) {
            return ChessColor.WHITE;
        } else {
            return ChessColor.BLACK;
        }
    }


    public static void setTimeLeftsToMatchResult(ChessMatchResult matchResult, Match match, int currentMoveNumber) {
        ChessColor currentTurnUserColor = getUserColorByMoveNumber(currentMoveNumber);
        ChessColor enemyColor = ChessColor.getInvertedColor(currentTurnUserColor);
        TimeLefts timeLefts = calculateTimeLefts(match, currentMoveNumber, currentTurnUserColor,
                enemyColor);

        matchResult.setTimeLeftByUserColor(currentTurnUserColor, timeLefts.getNewTimeLeft());
        matchResult.setTimeLeftByUserColor(enemyColor, timeLefts.getEnemyTimeLeft());
    }

    public static TimeLefts calculateTimeLefts(Match match, int currentMoveNumber,
                                               ChessColor currentTurnUserColor, ChessColor enemyColor) {
        long newTimeLeft;
        long enemyTimeLeft;
        long thisMoveTime = System.currentTimeMillis();

        if (currentMoveNumber < 2) {
            newTimeLeft = ChessGameConstants.BLITZ_INITIAL_TIME_LEFT_MS;
            enemyTimeLeft = ChessGameConstants.BLITZ_INITIAL_TIME_LEFT_MS;
        } else {
            ChessMove prevMove = match.getChessMovesRecord().get(currentMoveNumber - 1);
            long timeLeft = prevMove.getTimeLeftByUserColor(currentTurnUserColor);
            newTimeLeft = timeLeft - (thisMoveTime - match.getLastMoveTimeMS());
            newTimeLeft = newTimeLeft < 0 ? 0 : newTimeLeft;
            enemyTimeLeft = prevMove.getTimeLeftByUserColor(enemyColor);
        }

        return new TimeLefts(newTimeLeft, enemyTimeLeft, thisMoveTime);
    }


    public static String boardStateToSimplifiedBoardStateString(ChessPiece[][] boardState) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_LENGTH; j++) {
                if (boardState[i][j] == null) {
                    stringBuilder.append("n");
                } else {
                    stringBuilder.append(getChessPieceTypeOrdinal(boardState[i][j]));
                }
            }
        }
        return stringBuilder.toString();
    }

    private static int getChessPieceTypeOrdinal(ChessPiece chessPiece) {
        Class<?> chessPieceClass = chessPiece.getClass();

        switch (chessPieceClass.getSimpleName()) {
            case "Bishop":
                return 0;
            case "King":
                return 1;
            case "Knight":
                return 2;
            case "Pawn":
                return 3;
            case "Queen":
                return 4;
            case "Rook":
                return 5;
            default:
                throw new RuntimeException("Incorrect Chess Piece class: " + chessPieceClass);
        }
    }

}
