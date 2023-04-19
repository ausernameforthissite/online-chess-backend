package tsar.alex.model;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChessCoords {
    @Size(min = 0, max = 7, message = "numberCoord must be between 0 and 7")
    private int numberCoord;

    @Size(min = 0, max = 7, message = "letterCoord must be between 0 and 7")
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
