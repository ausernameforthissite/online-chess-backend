package tsar.alex.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserRatingId implements Serializable {
    private String username;
    private ChessGameType chessGameType;
}
