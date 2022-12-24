package tsar.alex.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindMatchResult {
    private boolean success;
    private FindMatchResponse response;
    private FindMatchError error;

    public FindMatchResult(Long matchId) {
        success = true;
        response = new FindMatchResponse(matchId);
    }

    public FindMatchResult(String errorMessage) {
        error = new FindMatchError(errorMessage);
    }
}
