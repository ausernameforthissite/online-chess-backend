package tsar.alex.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="chess_match_user_ratings_record")
@ToString
public class ChessMatchUserRatingsRecord {
    @Id
    @Column(name = "match_id")
    private long matchId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished")
    private boolean finished;

    @Column(name = "white_username")
    private String whiteUsername;

    @Column(name = "white_initial_rating")
    private int whiteInitialRating;

    @Column(name = "white_rating_change")
    private int whiteRatingChange;

    @Column(name = "black_username")
    private String blackUsername;

    @Column(name = "black_initial_rating")
    private int blackInitialRating;

    @Column(name = "black_rating_change")
    private int blackRatingChange;

    @Column(name = "technical_finish")
    private boolean technicalFinish;

    @Column(name = "draw")
    private boolean draw;

    @Column(name = "winner_color")
    @Enumerated(EnumType.STRING)
    private ChessColor winnerColor;


    public int getInitialRatingByUserColorIndexNumber(int i) {
        if (i == 0) {
            return whiteInitialRating;
        } else if (i == 1) {
            return blackInitialRating;
        } else {
            throw new RuntimeException("Expected i = 0 or 1. Got i = " + i);
        }
    }
}
