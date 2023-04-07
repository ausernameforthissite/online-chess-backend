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
    public boolean makeMoveIfPossible(Match match, ChessMove chessMove) {
        chessMove.setPreviousEnPassantCoords(match.getEnPassantPawnCoords());

        ChessPiece[][] boardState = match.getBoardState();

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

        boolean result = goForward(match, chessMove, direction, kingCoords);
        if (!result) {
            result = capture(match, chessMove, kingCoords);
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

    private boolean goForward(Match match, ChessMove chessMove, int direction, ChessCoords kingCoords) {
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

        ChessPiece[][] boardState = match.getBoardState();
        if (boardState[startNumberCoord + direction][letterCoord] != null) {
            return false;
        }

        int deltaNumberCoords = endNumberCoord - startNumberCoord;

        if (deltaNumberCoords * direction == 1) {
            if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
                changeBoardStateOneMove(boardState, startCoords, endCoords);
                match.setEnPassantPawnCoords(null);
                return true;
            }
            return false;
        }

        if (this.firstMove && deltaNumberCoords * direction == 2 && boardState[endNumberCoord][letterCoord] == null) {
            if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
                changeBoardStateOneMove(boardState, startCoords, endCoords);
                match.setEnPassantPawnCoords(endCoords);
                return true;
            }
        }

        return false;
    }

    private boolean capture(Match match, ChessMove chessMove, ChessCoords kingCoords) {
        ChessCoords startCoords = chessMove.getStartCoords();
        ChessCoords endCoords = chessMove.getEndCoords();
        ChessPiece[][] boardState = match.getBoardState();
        int startNumberCoord = startCoords.getNumberCoord();
        int endNumberCoord = endCoords.getNumberCoord();
        int endLetterCoord = endCoords.getLetterCoord();

        if (isAttackingField(boardState, startCoords, endCoords)) {
            if (boardState[endNumberCoord][endLetterCoord] != null) {
                if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
                    match.setEnPassantPawnCoords(null);

                    chessMove.setEndPieceFirstMove(isEndPieceFirstMove(boardState[endCoords.getNumberCoord()][endCoords.getLetterCoord()]));

                    changeBoardStateOneMove(boardState, startCoords, endCoords);
                    return true;
                }
                return false;
            } else if (boardState[startNumberCoord][endLetterCoord] instanceof Pawn
                        && new ChessCoords(startNumberCoord, endLetterCoord).equals(match.getEnPassantPawnCoords())) {
                return captureEnPassant(match, startCoords, endCoords, kingCoords);
            }
        }

        return false;
    }

    private boolean captureEnPassant(Match match, ChessCoords startCoords, ChessCoords endCoords, ChessCoords kingCoords) {
        int startNumberCoord = startCoords.getNumberCoord();
        int endLetterCoord = endCoords.getLetterCoord();

        ChessPiece[][] boardState = match.getBoardState();
        ChessPiece[][] tempBoardState = cloneBoardState(boardState);
        changeBoardStateOneMove(tempBoardState, startCoords, endCoords);
        tempBoardState[startNumberCoord][endLetterCoord] = null;

        if (isUnderAttack(tempBoardState, kingCoords, this.color)) {
            return false;
        } else {
            match.setEnPassantPawnCoords(null);
            changeBoardStateOneMove(boardState, startCoords, endCoords);
            boardState[startNumberCoord][endLetterCoord] = null;
            return true;
        }
    }


    @Override
    protected boolean doesPieceHavePossibleMoves(Match match, ChessCoords startCoords, ChessCoords kingCoords) {
        int direction = getPawnDirection();



        if (checkGoForward(match.getBoardState(), startCoords, direction, kingCoords)) {
            return true;
        }

        return checkCapture(match, startCoords, direction, kingCoords);
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

    private boolean checkCapture(Match match, ChessCoords startCoords, int direction, ChessCoords kingCoords) {
        ChessPiece[][] boardState = match.getBoardState();

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
                    && new ChessCoords(startNumberCoord, newLetterCoord).equals(match.getEnPassantPawnCoords())) {

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
