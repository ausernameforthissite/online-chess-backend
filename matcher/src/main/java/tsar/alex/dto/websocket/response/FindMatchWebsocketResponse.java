package tsar.alex.dto.websocket.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class FindMatchWebsocketResponse {
    private FindMatchWebsocketResponseEnum type;
}
