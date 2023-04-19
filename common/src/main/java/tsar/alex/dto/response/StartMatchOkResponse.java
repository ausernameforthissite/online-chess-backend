package tsar.alex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartMatchOkResponse implements StartMatchResponse, RestApiOkResponse {
    private long matchId;
    private Instant startedAt;
    private boolean sameUsersOrder;
}
