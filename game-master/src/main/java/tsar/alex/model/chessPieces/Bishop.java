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
public class Bishop extends ChessPiece {

    public Bishop() {
    }

    public Bishop(ChessColor color) {
        super(color);
    }


    @Override
    protected boolean isAttackingField(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords) {
        return ChessPiece.isAttackingFieldDiagonal(boardState, startCoords, endCoords);
    }

    @Override
    protected boolean doesPieceHavePossibleMoves(Game game, ChessCoords startCoords, ChessCoords kingCoords) {
        return checkGoDiagonal(game.getBoardState(), startCoords, kingCoords, this.color);
    }
}
