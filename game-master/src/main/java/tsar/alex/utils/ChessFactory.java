package tsar.alex.utils;

import tsar.alex.model.ChessColor;
import tsar.alex.model.ChessPiece;
import tsar.alex.model.ChessPieceEnum;
import tsar.alex.model.chessPieces.*;


public class ChessFactory {

    public static final ChessPiece[][] INITIAL_BOARD_STATE;

    static {
        ChessPiece[][] boardStateArray = new ChessPiece[8][8];

        for (int i = 0; i < 8; i++) {
            boardStateArray[1][i] = new Pawn(ChessColor.WHITE, true);
            boardStateArray[6][i] = new Pawn(ChessColor.BLACK, true);
        }

        boardStateArray[0][0] = new Rook(ChessColor.WHITE, true);
        boardStateArray[0][1] = new Knight(ChessColor.WHITE);
        boardStateArray[0][2] = new Bishop(ChessColor.WHITE);
        boardStateArray[0][3] = new Queen(ChessColor.WHITE);
        boardStateArray[0][4] = new King(ChessColor.WHITE, true);
        boardStateArray[0][5] = new Bishop(ChessColor.WHITE);
        boardStateArray[0][6] = new Knight(ChessColor.WHITE);
        boardStateArray[0][7] = new Rook(ChessColor.WHITE, true);

        boardStateArray[7][0] = new Rook(ChessColor.BLACK, true);
        boardStateArray[7][1] = new Knight(ChessColor.BLACK);
        boardStateArray[7][2] = new Bishop(ChessColor.BLACK);
        boardStateArray[7][3] = new Queen(ChessColor.BLACK);
        boardStateArray[7][4] = new King(ChessColor.BLACK, true);
        boardStateArray[7][5] = new Bishop(ChessColor.BLACK);
        boardStateArray[7][6] = new Knight(ChessColor.BLACK);
        boardStateArray[7][7] = new Rook(ChessColor.BLACK, true);

        INITIAL_BOARD_STATE = boardStateArray;
    }

    public static ChessPiece getPromotedPawn(ChessPieceEnum chessPieceType, ChessColor color) {
        return switch (chessPieceType) {
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color, false);
            default -> throw new RuntimeException();
        };
    }
}
