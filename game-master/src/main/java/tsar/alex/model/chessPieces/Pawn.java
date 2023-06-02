package tsar.alex.model.chessPieces;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.model.*;
import tsar.alex.utils.ChessFactory;
import tsar.alex.utils.ChessGameUtils;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;

@Getter
@Setter
@ToString(callSuper=true)
public class Pawn extends ChessPiece {

    private boolean firstMove;

    public Pawn() {
    }

    public Pawn(ChessColor color, boolean firstMove) {
        super(color);
        this.firstMove = firstMove;
    }

    @Override
    public boolean makeMoveIfPossible(Game game, ChessMove chessMove) {
        chessMove.setPreviousEnPassantCoords(game.getEnPassantPawnCoords());

        ChessPiece[][] boardState = game.getBoardState();

        final int direction = getPawnDirection();
        boolean pawnWillBePromoted = chessMove.getPawnPromotionPiece() != null;
        int endNumberCoord = 0;

        if (pawnWillBePromoted) {
            endNumberCoord = direction < 0 ? 0 : BOARD_LENGTH - 1;
            if (endNumberCoord != chessMove.getEndCoords().getNumberCoord()) {
                return false;
            }
        }

        final ChessCoords kingCoords = findKingCoords(boardState, this.color);

        boolean result = goForward(game, chessMove, direction, kingCoords);

        if (!result) {
            result = capture(game, chessMove, kingCoords);
        }

        if (result) {
            chessMove.setStartPieceFirstMove(this.firstMove);
            this.firstMove = false;
            if (pawnWillBePromoted) {
                boardState[endNumberCoord][chessMove.getEndCoords().getLetterCoord()] =
                        ChessFactory.getPromotedPawn(chessMove.getPawnPromotionPiece(), this.color);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean goForward(Game game, ChessMove chessMove, int direction, ChessCoords kingCoords) {
        if (chessMove.getEndPiece() != null) {
            return false;
        }

        ChessCoords startCoords = chessMove.getStartCoords();
        ChessCoords endCoords = chessMove.getEndCoords();
        final int letterCoord = startCoords.getLetterCoord();
        final int startNumberCoord = startCoords.getNumberCoord();
        final int endNumberCoord = endCoords.getNumberCoord();

        if (letterCoord != endCoords.getLetterCoord()) {
            return false;
        }

        ChessPiece[][] boardState = game.getBoardState();
        if (boardState[startNumberCoord + direction][letterCoord] != null) {
            return false;
        }

        int deltaNumberCoords = endNumberCoord - startNumberCoord;

        if (deltaNumberCoords * direction == 1) {
            if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
                changeBoardStateOneMove(boardState, startCoords, endCoords);
                game.setEnPassantPawnCoords(null);
                return true;
            }
            return false;
        }

        if (this.firstMove && deltaNumberCoords * direction == 2 && boardState[endNumberCoord][letterCoord] == null) {
            if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
                changeBoardStateOneMove(boardState, startCoords, endCoords);
                game.setEnPassantPawnCoords(endCoords);
                return true;
            }
        }

        return false;
    }

    private boolean capture(Game game, ChessMove chessMove, ChessCoords kingCoords) {
        ChessCoords startCoords = chessMove.getStartCoords();
        ChessCoords endCoords = chessMove.getEndCoords();
        ChessPiece[][] boardState = game.getBoardState();
        int startNumberCoord = startCoords.getNumberCoord();
        int endNumberCoord = endCoords.getNumberCoord();
        int endLetterCoord = endCoords.getLetterCoord();

        if (isAttackingField(boardState, startCoords, endCoords)) {
            if (boardState[endNumberCoord][endLetterCoord] != null) {
                if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
                    game.setEnPassantPawnCoords(null);

                    chessMove.setEndPieceFirstMove(isEndPieceFirstMove(boardState[endCoords.getNumberCoord()][endCoords.getLetterCoord()]));

                    changeBoardStateOneMove(boardState, startCoords, endCoords);
                    return true;
                }
                return false;
            } else if (boardState[startNumberCoord][endLetterCoord] instanceof Pawn
                        && new ChessCoords(startNumberCoord, endLetterCoord).equals(game.getEnPassantPawnCoords())) {
                return captureEnPassant(game, startCoords, endCoords, kingCoords);
            }
        }

        return false;
    }

    private boolean captureEnPassant(Game game, ChessCoords startCoords, ChessCoords endCoords, ChessCoords kingCoords) {
        int startNumberCoord = startCoords.getNumberCoord();
        int endLetterCoord = endCoords.getLetterCoord();

        ChessPiece[][] boardState = game.getBoardState();
        ChessPiece[][] tempBoardState = cloneBoardState(boardState);
        changeBoardStateOneMove(tempBoardState, startCoords, endCoords);
        tempBoardState[startNumberCoord][endLetterCoord] = null;

        if (isUnderAttack(tempBoardState, kingCoords, this.color)) {
            return false;
        } else {
            game.setEnPassantPawnCoords(null);
            changeBoardStateOneMove(boardState, startCoords, endCoords);
            boardState[startNumberCoord][endLetterCoord] = null;
            return true;
        }
    }


    @Override
    protected boolean doesPieceHavePossibleMoves(Game game, ChessCoords startCoords, ChessCoords kingCoords) {
        int direction = getPawnDirection();



        if (checkGoForward(game.getBoardState(), startCoords, direction, kingCoords)) {
            return true;
        }

        return checkCapture(game, startCoords, direction, kingCoords);
    }

    private boolean checkGoForward(ChessPiece[][] boardState, ChessCoords startCoords, int direction, ChessCoords kingCoords) {
        int startNumberCoord = startCoords.getNumberCoord();
        int letterCoord = startCoords.getLetterCoord();
        int newNumberCoord;


        for (int i = 1; i <= (this.firstMove ? 2 : 1); i++) {
            newNumberCoord = startNumberCoord + i * direction;
            if (boardState[newNumberCoord][letterCoord] == null) {
                if (!willBeCheck(boardState, startCoords, new ChessCoords(newNumberCoord, letterCoord), kingCoords, this.color)) {
                    return true;
                }
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean checkCapture(Game game, ChessCoords startCoords, int direction, ChessCoords kingCoords) {
        ChessPiece[][] boardState = game.getBoardState();

        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        int newNumberCoord = startNumberCoord + direction;
        int newLetterCoord;

        for (int j = -1; j < 1; j += 2) {
            newLetterCoord = startLetterCoord + j;

            if (!ChessGameUtils.isCoordPossible(newLetterCoord)) {
                continue;
            }

            if (boardState[newNumberCoord][newLetterCoord] != null) {
                if (boardState[newNumberCoord][newLetterCoord].getColor() != this.color
                        && !willBeCheck(boardState, startCoords, new ChessCoords(newNumberCoord, newLetterCoord),
                        kingCoords, this.color)) {
                    return true;
                }
            } else if (boardState[startNumberCoord][newLetterCoord] instanceof Pawn
                    && new ChessCoords(startNumberCoord, newLetterCoord).equals(game.getEnPassantPawnCoords())) {

                ChessPiece[][] tempBoardState = cloneBoardState(boardState);
                changeBoardStateOneMove(tempBoardState, startCoords, new ChessCoords(newNumberCoord, newLetterCoord));
                tempBoardState[startNumberCoord][newLetterCoord] = null;

                if (!isUnderAttack(tempBoardState, kingCoords, this.color)) {
                    return true;
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

        final int direction = getPawnDirection();

        if (startNumberCoord + direction != endNumberCoord) {
            return false;
        }

        return startLetterCoord - 1 == endLetterCoord
                || startLetterCoord + 1 == endLetterCoord;
    }

    private int getPawnDirection() {
        return this.color == ChessColor.WHITE ? +1 : -1;
    }
}
