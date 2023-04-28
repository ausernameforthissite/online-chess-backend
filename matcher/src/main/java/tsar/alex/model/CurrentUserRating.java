package tsar.alex.model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Setter;
import tsar.alex.utils.EloRating;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "current_user_rating")
@IdClass(CurrentUserRatingId.class)
public class CurrentUserRating {

    @Id
    @Column(name = "username")
    private String username;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "chess_game_type")
    private ChessGameType chessGameType;

    @Column(name = "rating")
    private int rating;

    @Column(name = "matches_played")
    private int matchesPlayed;

    @Column(name = "k")
    private int K;


    public static CurrentUserRating getDefaultUserRating(String username, ChessGameType chessGameType) {
        return new CurrentUserRating(username, chessGameType, EloRating.DEFAULT_USER_RATING, 0, EloRating.K_VALUES[0]);
    }

    public int incrementMatchesPlayed() {
        return ++this.matchesPlayed;
    }

}
