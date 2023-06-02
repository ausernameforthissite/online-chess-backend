package tsar.alex.dto;

import tsar.alex.dto.response.GeneralBadResponse;


public class GameStateBadResponse extends GeneralBadResponse implements GameStateResponse {
    public GameStateBadResponse(String message) {
        super(message);
    }
}