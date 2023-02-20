package tsar.alex.utils;

import tsar.alex.model.*;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;

public class ChessGameUtils {

    public static boolean isCoordPossible(int coord) {
        if (coord >= 0 && coord < BOARD_LENGTH) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean areCoordsPossible(ChessCoords coords) {
        return isCoordPossible(coords.getNumberCoord()) && isCoordPossible(coords.getLetterCoord());
    }

    public static boolean validateChessMove(ChessMove chessMove, ChessCoords startCoords, ChessColor userColor, ChessPiece[][] boardState) {
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

        if (!startPiece.getClass().getSimpleName().equals(chessMove.getStartPiece().getName())) {
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

            if (!endPiece.getClass().getSimpleName().equals(chessMove.getEndPiece().getName())) {
                return false;
            }

            if (userColor == endPiece.getColor()) {
                return false;
            }
        }

        return true;
    }
}
