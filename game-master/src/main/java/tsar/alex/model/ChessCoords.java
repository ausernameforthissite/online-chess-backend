package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChessCoords {
    private int numberCoord;
    private int letterCoord;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessCoords that = (ChessCoords) o;
        return letterCoord == that.letterCoord && numberCoord == that.numberCoord;
    }

    @Override
    public int hashCode() {
        return Objects.hash(letterCoord, numberCoord);
    }

    @Override
    public Object clone() {
        ChessCoords chessCoords = null;
        try {
            chessCoords = (ChessCoords) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Exception"); //TODO
        }
        return chessCoords;
    }
}
