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
@JsonTypeInfo(  use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FindMatchCancelRequest.class, name = "CANCEL"),
})
public abstract class FindMatchWebsocketRequest {

    private FindMatchWebsocketRequestEnum type;

}
