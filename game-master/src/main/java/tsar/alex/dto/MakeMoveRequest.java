package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tsar.alex.model.ChessMove;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MakeMoveRequest {
    private Long matchId;
    private ChessMove chessMove;
}
