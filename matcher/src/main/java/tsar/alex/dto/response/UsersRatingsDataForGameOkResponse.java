package tsar.alex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersRatingsDataForGameOkResponse implements UsersRatingsDataForGameResponse, RestApiOkResponse {
    private int whiteInitialRating;
    private Integer whiteRatingChange;
    private int blackInitialRating;
    private Integer blackRatingChange;
}
