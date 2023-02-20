package tsar.alex.model.chessPieces;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.model.*;

@Getter
@Setter
@ToString(callSuper=true)
public class Rook extends ChessPiece {

    private boolean firstMove;

    public Rook() {
    }

    public Rook(ChessColor color) {
        super(color);
    }

    public Rook(ChessColor color, boolean firstMove) {
        super(color);
        this.firstMove = firstMove;
    }

    @Override
    public boolean makeMoveIfPossible(Match match, ChessMove chessMove) {
        if (super.makeMoveIfPossible(match, chessMove)) {
            chessMove.setStartPieceFirstMove(this.firstMove);
            this.firstMove = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean doesPieceHavePossibleMoves(Match match, ChessCoords startCoords, ChessCoords kingCoords) {
        ChessPiece[][] boardState = match.getBoardState();
        return checkGoVertical(boardState, startCoords, kingCoords, this.color)
                || checkGoHorizontal(boardState, startCoords, kingCoords, this.color);
    }

    @Override
    protected boolean isAttackingField(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords) {
        return ChessPiece.isAttackingFieldVertical(boardState, startCoords, endCoords) ||
                ChessPiece.isAttackingFieldHorizontal(boardState, startCoords, endCoords);
    }
}
