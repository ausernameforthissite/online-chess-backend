package tsar.alex.dto;

import tsar.alex.dto.response.GeneralBadResponse;


public class MatchStateBadResponse extends GeneralBadResponse implements MatchStateResponse {
    public MatchStateBadResponse(String message) {
        super(message);
    }
}