package tsar.alex.dto;

import lombok.*;
import tsar.alex.model.ChessColor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateUsersRatingsRequest {
    private long matchId;
    private boolean technicalFinish;
    private boolean draw;
    private ChessColor winnerColor;
}
