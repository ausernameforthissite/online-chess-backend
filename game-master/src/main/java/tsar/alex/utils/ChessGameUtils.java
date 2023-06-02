package tsar.alex.utils;

import javax.validation.constraints.NotNull;
import tsar.alex.dto.TimeLefts;
import tsar.alex.model.*;
import tsar.alex.model.chessPieces.*;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;
import static tsar.alex.utils.CommonTextConstants.INCORRECT_CHESS_PIECE_CLASS;

public class ChessGameUtils {

    private static final Class<?>[] CHESS_PIECE_CLASSES_ARRAY = new Class[]{Bishop.class, King.class, Knight.class,
            Pawn.class, Queen.class, Rook.class};

    public static boolean isCoordPossible(int coord) {
        return coord >= 0 && coord < BOARD_LENGTH;
    }

    public static boolean areCoordsPossible(ChessCoords coords) {
        return isCoordPossible(coords.getNumberCoord()) && isCoordPossible(coords.getLetterCoord());
    }

    public static ChessColor getChessSquareColorByCoords(int numberCoord, int letterCoord) {
        return (numberCoord + letterCoord) % 2 == 1 ? ChessColor.WHITE : ChessColor.BLACK;
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

            if (Math.abs(chessMove.getCastling()) > 1) {
                return false;
            }

            if (startCoords.getNumberCoord() != endCoords.getNumberCoord()) {
                return false;
            }
        }

        if (chessMove.getPawnPromotionPiece() != null) {
            if (chessMove.getStartPiece() != ChessPieceEnum.PAWN) {
                return false;
            }

            if (chessMove.getPawnPromotionPiece() == ChessPieceEnum.PAWN
                    || chessMove.getPawnPromotionPiece() == ChessPieceEnum.KING) {
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

    public static String getCurrentTurnUsername(UsersInGame usersInGame, int currentMoveNumber) {
        if (currentMoveNumber % 2 == 0) {
            return usersInGame.getWhiteUsername();
        } else {
            return usersInGame.getBlackUsername();
        }
    }

    public static ChessColor getUserColorByMoveNumber(int currentMoveNumber) {
        if (currentMoveNumber % 2 == 0) {
            return ChessColor.WHITE;
        } else {
            return ChessColor.BLACK;
        }
    }


    public static void setTimeLeftsToGameResult(ChessGameResult gameResult, Game game, int currentMoveNumber) {
        ChessColor currentTurnUserColor = getUserColorByMoveNumber(currentMoveNumber);
        ChessColor enemyColor = ChessColor.getInvertedColor(currentTurnUserColor);
        TimeLefts timeLefts = calculateTimeLefts(game, true, currentMoveNumber, currentTurnUserColor,
                enemyColor);

        gameResult.setTimeLeftByUserColor(currentTurnUserColor, timeLefts.getNewTimeLeft());
        gameResult.setTimeLeftByUserColor(enemyColor, timeLefts.getEnemyTimeLeft());
    }

    public static TimeLefts calculateTimeLefts(Game game, boolean finished, int currentMoveNumber,
            ChessColor currentTurnUserColor, ChessColor enemyColor) {
        ChessGameTypeWithTimings gameType = game.getGameType();
        long newTimeLeft;
        long enemyTimeLeft;
        long thisMoveTime = System.currentTimeMillis();

        if (currentMoveNumber < 2) {
            long initialTime = gameType.getInitialTimeMS();
            newTimeLeft = initialTime;
            enemyTimeLeft = initialTime;
        } else {
            long timeIncrement = finished ? 0 : gameType.getTimeIncrementMS();
            ChessMove prevMove = game.getChessMovesRecord().get(currentMoveNumber - 1);
            long timeLeft = prevMove.getTimeLeftByUserColor(currentTurnUserColor);
            newTimeLeft = timeLeft - (thisMoveTime - game.getLastMoveTimeMS()) + timeIncrement;
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
                    stringBuilder.append((char) 120);
                } else {
                    stringBuilder.append(getChessPieceTypeOrdinal(boardState[i][j]));
                }
            }
        }
        return stringBuilder.toString();
    }

    private static char getChessPieceTypeOrdinal(@NotNull ChessPiece chessPiece) {
        Class<?> chessPieceClass = chessPiece.getClass();

        for (int i = 0; i < CHESS_PIECE_CLASSES_ARRAY.length; i++) {
            if (CHESS_PIECE_CLASSES_ARRAY[i].equals(chessPieceClass)) {
                return (char) (i + (chessPiece.getColor() == ChessColor.WHITE ? 100 : 110));
            }
        }

        throw new RuntimeException(String.format(INCORRECT_CHESS_PIECE_CLASS, chessPieceClass));
    }

}
