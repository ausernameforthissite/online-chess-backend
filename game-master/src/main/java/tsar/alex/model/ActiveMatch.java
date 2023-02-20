package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveMatch {

    private long currentTurnUserId;
    private Map<Long, UsersInMatch> usersInMatch;

}
