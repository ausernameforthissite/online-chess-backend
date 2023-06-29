package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsersInGame {

    private String whiteUsername;
    private String blackUsername;

    public ChessColor getColorByUsername(@NotNull String username) {
        if (username.equals(whiteUsername)) {
            return ChessColor.WHITE;
        } else if (username.equals(blackUsername)) {
            return ChessColor.BLACK;
        } else {
            return null;
        }
    }
}
