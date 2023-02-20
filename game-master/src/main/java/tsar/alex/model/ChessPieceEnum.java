package tsar.alex.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum ChessPieceEnum {
    BISHOP("bishop"),
    KING("king"),
    KNIGHT("knight"),
    PAWN("pawn"),
    QUEEN("queen"),
    ROOK("rook");

    private String name;

    @JsonValue
    public String getName() {
        return name;
    }
}
