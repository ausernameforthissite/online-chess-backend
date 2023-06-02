package tsar.alex.dto.request;

import javax.validation.constraints.NotNull;
import lombok.*;
import tsar.alex.model.ChessColor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUsersRatingsRequest {
    @NotNull(message = "gameId is null")
    private String gameId;
    private boolean technicalFinish;
    private boolean draw;
    private ChessColor winnerColor;
}
