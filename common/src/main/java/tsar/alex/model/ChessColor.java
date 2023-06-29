package tsar.alex.model;

import static tsar.alex.utils.CommonTextConstants.ILLEGAL_ARGUMENT;

public enum ChessColor {
    WHITE,
    BLACK;

    public static ChessColor getInvertedColor(ChessColor inputColor) {
        if (inputColor == WHITE) return BLACK;
        if (inputColor == BLACK) return WHITE;
        throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT, "inputColor", inputColor));
    }
}
