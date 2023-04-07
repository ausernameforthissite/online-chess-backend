package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import tsar.alex.dto.websocket.request.FindMatchCancelRequest;
import tsar.alex.dto.websocket.request.FindMatchWebsocketRequest;
import tsar.alex.utils.websocket.UsersWaitingForMatchWebsocketHolder;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class FindMatchWebsocketController {

    private final UsersWaitingForMatchWebsocketHolder UWFMWebsocketHolder;

    @MessageMapping("/find_match/request")
    public void handleFindMatchWebsocketMessage(FindMatchWebsocketRequest request, Principal user) {
        if (request instanceof FindMatchCancelRequest) {
            UWFMWebsocketHolder.cancelSearchIfPossible(user.getName());
        }
    }
}
