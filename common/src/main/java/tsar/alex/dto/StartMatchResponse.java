package tsar.alex.dto;

import lombok.Data;
import lombok.ToString;
import tsar.alex.enums.StartMatchEnum;
import tsar.alex.model.Pair;

@Data
@ToString
public class StartMatchResponse {
    private Long matchId;

    private Pair<StartMatchEnum> usersResponses;

    public StartMatchResponse() {
        matchId = -1L;
        usersResponses = new Pair<>(new StartMatchEnum[2]);
    }

    public StartMatchResponse(StartMatchEnum user0Response, StartMatchEnum user1Response) {
        matchId = -1L;
        usersResponses = new Pair<>(user0Response, user1Response);
    }
}
