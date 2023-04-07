package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersInMatch {

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
