package tsar.alex.model;

public enum ChessColor {
    WHITE,
    BLACK;

    public static ChessColor getInvertedColor(ChessColor inputColor) {
        if (inputColor == WHITE) {
            return BLACK;
        } else {
            return WHITE;
        }
    }
}
