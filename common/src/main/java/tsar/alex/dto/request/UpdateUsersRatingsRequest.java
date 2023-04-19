package tsar.alex.dto.request;

import javax.validation.constraints.NotNull;
import lombok.*;
import tsar.alex.model.ChessColor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUsersRatingsRequest {
    @NotNull(message = "matchId is null")
    private long matchId;
    private boolean technicalFinish;
    private boolean draw;
    private ChessColor winnerColor;
}
