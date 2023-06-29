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
import lombok.ToString;
import tsar.alex.utils.EloRatingCalculator;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "current_user_rating")
@IdClass(CurrentUserRatingId.class)
@ToString
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

    @Column(name = "games_played")
    private int gamesPlayed;

    @Column(name = "k")
    private int K;


    public static CurrentUserRating getDefaultUserRating(String username, ChessGameType chessGameType) {
        return new CurrentUserRating(username, chessGameType, EloRatingCalculator.DEFAULT_USER_RATING, 0,
                EloRatingCalculator.K_VALUES[0]);
    }

    public int incrementGamesPlayed() {
        return ++this.gamesPlayed;
    }

}
