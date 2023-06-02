package tsar.alex.model.chessPieces;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.model.ChessColor;
import tsar.alex.model.ChessCoords;
import tsar.alex.model.ChessPiece;
import tsar.alex.model.Game;

@Getter
@Setter
@ToString(callSuper=true)
public class Queen extends ChessPiece {

    public Queen() {
    }

    public Queen(ChessColor color) {
        super(color);
    }

    @Override
    protected boolean doesPieceHavePossibleMoves(Game game, ChessCoords startCoords, ChessCoords kingCoords) {
        ChessPiece[][] boardState = game.getBoardState();
        return checkGoDiagonal(boardState, startCoords, kingCoords, this.color)
                || checkGoVertical(boardState, startCoords, kingCoords, this.color)
                || checkGoHorizontal(boardState, startCoords, kingCoords, this.color);
    }

    @Override
    protected boolean isAttackingField(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords) {
        return ChessPiece.isAttackingFieldDiagonal(boardState, startCoords, endCoords) ||
                ChessPiece.isAttackingFieldVertical(boardState, startCoords, endCoords) ||
                ChessPiece.isAttackingFieldHorizontal(boardState, startCoords, endCoords);
    }
}
