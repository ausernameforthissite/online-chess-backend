package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersRatingsDataForMatchOkResponse implements UsersRatingsDataForMatchResponse {
    private int whiteInitialRating;
    private Integer whiteRatingChange;
    private int blackInitialRating;
    private Integer blackRatingChange;
}
