package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersRatingsDataForMatchBadResponse implements UsersRatingsDataForMatchResponse {
    private String message;
}
