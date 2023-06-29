package tsar.alex.dto.websocket.response;

import lombok.*;
import tsar.alex.dto.WebsocketResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class FindGameWebsocketResponse implements WebsocketResponse {
    private FindGameWebsocketResponseEnum type;
}
