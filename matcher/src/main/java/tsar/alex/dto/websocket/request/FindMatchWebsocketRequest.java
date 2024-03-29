package tsar.alex.dto.websocket.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FindMatchCancelRequest.class, name = "CANCEL"),
})
public abstract class FindMatchWebsocketRequest {
}
