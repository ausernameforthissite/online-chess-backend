package tsar.alex.dto.websocket.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class FindGameWebsocketResponse {
    private FindGameWebsocketResponseEnum type;
}
