package tsar.alex.model.chessPieces;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.model.*;
import tsar.alex.utils.ChessGameUtils;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;

@Getter
@Setter
@ToString(callSuper=true)
public class King extends ChessPiece {

    private boolean firstMove;

    public King() {
    }

    public King(ChessColor color, boolean firstMove) {
        super(color);
        this.firstMove = firstMove;
    }


    @Override
    public boolean makeMoveIfPossible(Match match, ChessMove chessMove) {
        if (chessMove.getCastling() == 0) {
            if (super.makeMoveIfPossible(match, chessMove)) {
                chessMove.setStartPieceFirstMove(this.firstMove);
                this.firstMove = false;
                return true;
            } else {
                return false;
            }
        } else {
            return doCastling(match, chessMove);
        }
    }

    private boolean doCastling(Match match, ChessMove chessMove) {
        if (this.firstMove) {

            ChessCoords startCoords = chessMove.getStartCoords();
            ChessCoords endCoords = chessMove.getEndCoords();

            int numberCoord = startCoords.getNumberCoord();
            int startLetterCoord = startCoords.getLetterCoord();
            int endLetterCoord = endCoords.getLetterCoord();
            int deltaLetterCoord = endLetterCoord - startLetterCoord;
            int sign = chessMove.getCastling();

            if (deltaLetterCoord != sign * 2) {
                return false;
            }

            int rookInitialLetterCoord = sign < 0 ? 0 : BOARD_LENGTH - 1;
            ChessPiece[][] boardState = match.getBoardState();

            if (boardState[numberCoord][rookInitialLetterCoord] instanceof Rook myRook
                    && myRook.getColor() == this.color && myRook.isFirstMove()) {

                int j = startLetterCoord;

                while (j != rookInitialLetterCoord - sign) {
                    j += sign;
                    if (boardState[numberCoord][j] != null) {
                        return false;
                    }
                }

                j = startLetterCoord - sign;

                while (j != endLetterCoord) {
                    j += sign;
                    if (isUnderAttack(boardState, new ChessCoords(numberCoord, j), this.color)) {
                        return false;
                    }
                }

                match.setEnPassantPawnCoords(null);
                boardState[numberCoord][endLetterCoord] = this;
                boardState[numberCoord][endLetterCoord - sign] = myRook;
                this.firstMove = false;
                myRook.setFirstMove(false);
                boardState[numberCoord][startLetterCoord] = null;
                boardState[numberCoord][rookInitialLetterCoord] = null;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean doesPieceHavePossibleMoves(Match match, ChessCoords startCoords, ChessCoords kingCoords) {
        ChessPiece[][] boardState = match.getBoardState();

        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        int newNumberCoord;
        int newLetterCoord;

        for (int i = -1; i <= 1; i++) {
            newNumberCoord = startNumberCoord + i;

            if (!ChessGameUtils.isCoordPossible(newNumberCoord)) {
                continue;
            }

            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                newLetterCoord = startLetterCoord + j;

                if (!ChessGameUtils.isCoordPossible(newLetterCoord)) {
                    continue;
                }


                if (!(boardState[newNumberCoord][newLetterCoord] != null
                    && boardState[newNumberCoord][newLetterCoord].getColor() == this.color)) {
                    ChessCoords endCoords = new ChessCoords(newNumberCoord, newLetterCoord);
                    if (!willBeCheck(boardState, startCoords, endCoords, endCoords, this.color)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    @Override
    protected boolean isAttackingField(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords) {
        return Math.abs(endCoords.getNumberCoord() - startCoords.getNumberCoord()) <= 1
                && Math.abs(endCoords.getLetterCoord() - startCoords.getLetterCoord()) <= 1;
    }
}
