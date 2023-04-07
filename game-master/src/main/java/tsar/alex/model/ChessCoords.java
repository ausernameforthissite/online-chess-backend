package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

}
