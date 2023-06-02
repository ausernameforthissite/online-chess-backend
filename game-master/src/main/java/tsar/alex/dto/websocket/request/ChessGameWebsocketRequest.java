package tsar.alex.dto.websocket.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChessGameInfoRequest.class, name = "INFO"),
        @JsonSubTypes.Type(value = ChessGameMoveRequest.class, name = "CHESS_MOVE"),
        @JsonSubTypes.Type(value = ChessGameDrawRequest.class, name = "DRAW"),
        @JsonSubTypes.Type(value = ChessGameAcceptDrawRequest.class, name = "ACCEPT_DRAW"),
        @JsonSubTypes.Type(value = ChessGameRejectDrawRequest.class, name = "REJECT_DRAW"),
        @JsonSubTypes.Type(value = ChessGameSurrenderRequest.class, name = "SURRENDER"),
})
public abstract class ChessGameWebsocketRequest {
    private ChessGameWebsocketRequestEnum type;
}
