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
        @JsonSubTypes.Type(value = ChessMatchInfoRequest.class, name = "INFO"),
        @JsonSubTypes.Type(value = ChessMatchMoveRequest.class, name = "CHESS_MOVE"),
        @JsonSubTypes.Type(value = ChessMatchDrawRequest.class, name = "DRAW"),
        @JsonSubTypes.Type(value = ChessMatchAcceptDrawRequest.class, name = "ACCEPT_DRAW"),
        @JsonSubTypes.Type(value = ChessMatchRejectDrawRequest.class, name = "REJECT_DRAW"),
        @JsonSubTypes.Type(value = ChessMatchSurrenderRequest.class, name = "SURRENDER"),
})
public abstract class ChessMatchWebsocketRequest {
    private ChessMatchWebsocketRequestEnum type;
}
