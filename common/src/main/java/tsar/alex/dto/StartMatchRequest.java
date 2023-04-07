package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tsar.alex.model.Pair;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StartMatchRequest {
    private Pair<String> pairOfUsernames;
}
