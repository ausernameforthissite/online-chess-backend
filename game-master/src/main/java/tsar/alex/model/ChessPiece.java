package tsar.alex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import tsar.alex.model.chessPieces.*;
import tsar.alex.utils.ChessGameUtils;

import static tsar.alex.utils.ChessGameConstants.BOARD_LENGTH;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(  use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Bishop.class, name = "bishop"),
    @JsonSubTypes.Type(value = King.class, name = "king"),
    @JsonSubTypes.Type(value = Knight.class, name = "knight"),
    @JsonSubTypes.Type(value = Pawn.class, name = "pawn"),
    @JsonSubTypes.Type(value = Queen.class, name = "queen"),
    @JsonSubTypes.Type(value = Rook.class, name = "rook"),
})
@ToString
public abstract class ChessPiece implements Cloneable {
    protected ChessColor color;

    protected abstract boolean doesPieceHavePossibleMoves(Game game, ChessCoords startCoords, ChessCoords kingCoords);

    protected abstract boolean isAttackingField(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords);


    // +
    public boolean makeMoveIfPossible(Game game, ChessMove chessMove) {
        ChessPiece[][] boardState = game.getBoardState();

        ChessCoords startCoords = chessMove.getStartCoords();
        ChessCoords endCoords = chessMove.getEndCoords();

        if (!isAttackingField(boardState, startCoords, endCoords)) {
            return false;
        }

        ChessCoords kingCoords = this instanceof King ? endCoords : findKingCoords(boardState, this.color);

        if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, this.color)) {
            chessMove.setPreviousEnPassantCoords(game.getEnPassantPawnCoords());

            game.setEnPassantPawnCoords(null);

            chessMove.setEndPieceFirstMove(isEndPieceFirstMove(
                                            boardState[endCoords.getNumberCoord()][endCoords.getLetterCoord()]));

            changeBoardStateOneMove(boardState, startCoords, endCoords);
            return true;
        }

        return false;
    }

    // +
    protected static boolean willBeCheck(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords,
                                  ChessCoords kingCoords, ChessColor startPieceColor) {
        ChessPiece[][] tempBoardState = cloneBoardState(boardState);
        changeBoardStateOneMove(tempBoardState, startCoords, endCoords);

        return isUnderAttack(tempBoardState, kingCoords, startPieceColor);
    }


    // +
    protected static boolean isAttackingFieldDiagonal(ChessPiece[][] boardState, ChessCoords startCoords,
                                                      ChessCoords endCoords) {
        final int deltaNumberCoord = endCoords.getNumberCoord() - startCoords.getNumberCoord();
        final int deltaLetterCoord = endCoords.getLetterCoord() - startCoords.getLetterCoord();

        if (Math.abs(deltaNumberCoord) != Math.abs(deltaLetterCoord)) {
            return false;
        } else {
            return isAttackingFieldLongMove(boardState, startCoords, endCoords,
                    (int) Math.signum(deltaNumberCoord), (int) Math.signum(deltaLetterCoord));
        }
    }

    // +
    protected static boolean isAttackingFieldVertical(ChessPiece[][] boardState, ChessCoords startCoords,
                                                      ChessCoords endCoords) {
        if (startCoords.getLetterCoord() != endCoords.getLetterCoord()) {
            return false;
        } else {
            return isAttackingFieldLongMove(boardState, startCoords, endCoords,
                    (int) Math.signum(endCoords.getNumberCoord() - startCoords.getNumberCoord()), 0);
        }
    }

    // +
    protected static boolean isAttackingFieldHorizontal(ChessPiece[][] boardState, ChessCoords startCoords,
                                                        ChessCoords endCoords) {
        if (startCoords.getNumberCoord() != endCoords.getNumberCoord()) {
            return false;
        } else {
            return isAttackingFieldLongMove(boardState, startCoords, endCoords, 0,
                    (int) Math.signum(endCoords.getLetterCoord() - startCoords.getLetterCoord()));
        }
    }

    // +
    private static boolean isAttackingFieldLongMove(ChessPiece[][] boardState, ChessCoords startCoords,
                                                    ChessCoords endCoords, int i, int j) {
        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        int endNumberCoord = endCoords.getNumberCoord();
        int endLetterCoord = endCoords.getLetterCoord();
        int newNumberCoord;
        int newLetterCoord;

        int k = 1;

        while (true) {
            newNumberCoord = startNumberCoord + i * k;
            newLetterCoord = startLetterCoord + j * k;

            if (newNumberCoord == endNumberCoord && newLetterCoord == endLetterCoord) {
                return true;
            }

            if (boardState[newNumberCoord][newLetterCoord] != null) {
                return false;
            }

            k++;
        }
    }



    public static boolean doesEnemyHavePossibleMoves(Game game, ChessCoords enemyKingCoords, ChessColor enemyColor) {
        ChessPiece[][] boardState = game.getBoardState();

        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_LENGTH; j++) {
                ChessPiece chessPiece = boardState[i][j];

                if (chessPiece != null && chessPiece.color == enemyColor
                    && chessPiece.doesPieceHavePossibleMoves(game, new ChessCoords(i, j), enemyKingCoords)) {
                    return true;
                }
            }
        }

        return false;
    }


    // +
    protected static boolean checkGoDiagonal(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords kingCoords,
                                             ChessColor startPieceColor) {
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j +=2) {
                if (checkGoLongMove(boardState, startCoords, kingCoords, i, j, startPieceColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    // +
    protected static boolean checkGoVertical(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords kingCoords,
                                             ChessColor startPieceColor) {
        for (int i = -1; i <= 1; i += 2) {
            if (checkGoLongMove(boardState, startCoords, kingCoords, i, 0, startPieceColor)) {
                return true;
            }
        }
        return false;
    }

    // +
    protected static boolean checkGoHorizontal(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords kingCoords,
                                               ChessColor startPieceColor) {
        for (int j = -1; j <= 1; j += 2) {
            if (checkGoLongMove(boardState, startCoords, kingCoords, 0, j, startPieceColor)) {
                return true;
            }
        }
        return false;
    }

    // +
    protected static boolean checkGoLongMove(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords kingCoords,
                                      int i, int j, ChessColor startPieceColor) {
        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        int newNumberCoord;
        int newLetterCoord;

        int k = 1;

        while (true) {
            newNumberCoord = startNumberCoord + i * k;
            newLetterCoord = startLetterCoord + j * k;

            ChessCoords endCoords = new ChessCoords(newNumberCoord, newLetterCoord);

            if (!ChessGameUtils.areCoordsPossible(endCoords)) {
                return false;
            }

            if (boardState[newNumberCoord][newLetterCoord] != null) {
                return boardState[newNumberCoord][newLetterCoord].color != startPieceColor
                        && !willBeCheck(boardState, startCoords, endCoords, kingCoords, startPieceColor);
            } else {
                if (!willBeCheck(boardState, startCoords, endCoords, kingCoords, startPieceColor)) {
                    return true;
                }
            }

            k++;
        }
    }



    // +
    public static ChessCoords findKingCoords(ChessPiece[][] boardState, ChessColor kingColor) {
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_LENGTH; j++) {
                if (boardState[i][j] instanceof King && boardState[i][j].getColor() == kingColor) {
                    return new ChessCoords(i, j);
                }
            }
        }
        throw new RuntimeException("King was not found"); // TODO
    }

    // +
    public static boolean isUnderAttack(ChessPiece[][] boardState, ChessCoords endCoords, ChessColor myColor) {
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_LENGTH; j++) {
                if (boardState[i][j] != null && boardState[i][j].getColor() != myColor
                        && boardState[i][j].isAttackingField(boardState, new ChessCoords(i, j), endCoords)) {
                    return true;
                }
            }
        }

        return false;
    }


    // +
    protected static void changeBoardStateOneMove(ChessPiece[][] boardState, ChessCoords startCoords, ChessCoords endCoords) {
        int startNumberCoord = startCoords.getNumberCoord();
        int startLetterCoord = startCoords.getLetterCoord();
        boardState[endCoords.getNumberCoord()][endCoords.getLetterCoord()] = boardState[startNumberCoord][startLetterCoord];
        boardState[startNumberCoord][startLetterCoord] = null;
    }

    protected static boolean isEndPieceFirstMove(ChessPiece endPiece) {
        return endPiece instanceof Pawn && ((Pawn) endPiece).isFirstMove() ||
                endPiece instanceof Rook && ((Rook) endPiece).isFirstMove();
    }

    // +
    protected static ChessPiece[][] cloneBoardState(ChessPiece[][] boardState) {
        ChessPiece[][] newBoardState = new ChessPiece[BOARD_LENGTH][BOARD_LENGTH];

        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_LENGTH; j++) {
                ChessPiece oldChessPiece = boardState[i][j];

                if (oldChessPiece == null) {
                    newBoardState[i][j] = null;
                } else {
                    newBoardState[i][j] = (ChessPiece) oldChessPiece.clone();
                }
            }
        }

        return newBoardState;
    }

    @Override
    public Object clone() {
        ChessPiece chessPiece = null;
        try {
            chessPiece = (ChessPiece) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return chessPiece;
    }

}


