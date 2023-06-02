package tsar.alex.model.chessPieces;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.model.ChessColor;
import tsar.alex.model.ChessCoords;
import tsar.alex.model.ChessPiece;
import tsar.alex.model.Game;
import tsar.alex.utils.ChessGameUtils;

@Getter
@Setter
@ToString(callSuper=true)
public class Knight extends ChessPiece {

    public Knight() {
    }

    public Knight(ChessColor color) {
        super(color);
    }

    @Override
    protected boolean doesPieceHavePossibleMoves(Game game, ChessCoords startCoords, ChessCoords kingCoords) {
        ChessPiece[][] boardState = game.getBoardState();

        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        int newNumberCoord;
        int newLetterCoord;

        for (int i = -2; i <= 2; i++) {
            if (i == 0) {
                continue;
            }

            newNumberCoord = startNumberCoord + i;

            if (!ChessGameUtils.isCoordPossible(newNumberCoord)) {
                continue;
            }

            for (int k = -1; k <= 1; k += 2) {
                newLetterCoord = startLetterCoord + k * (3 - Math.abs(i));

                if (!ChessGameUtils.isCoordPossible(newLetterCoord)) {
                    continue;
                }

                if (!(boardState[newNumberCoord][newLetterCoord] != null
                    && boardState[newNumberCoord][newLetterCoord].getColor() == this.color)) {

                    if (!willBeCheck(boardState, startCoords, new ChessCoords(newNumberCoord, newLetterCoord),
                            kingCoords, this.color)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected boolean isAttackingField(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords) {
        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        int endNumberCoord = endCoords.getNumberCoord();
        int endLetterCoord = endCoords.getLetterCoord();
        int newLetterCoord;
        int newNumberCoord;

        for (int i = -2; i <= 2; i++) {
            if (i == 0) {
                continue;
            }

            newNumberCoord = startNumberCoord + i;

            if (newNumberCoord == endNumberCoord) {
                for (int k = -1; k <= 1; k += 2) {
                    newLetterCoord = startLetterCoord + k * (3 - Math.abs(i));

                    if (newLetterCoord == endLetterCoord) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
