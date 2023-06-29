package tsar.alex.model;

import static tsar.alex.utils.CommonTextConstants.ILLEGAL_ARGUMENT;

import java.util.Objects;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="chess_game_user_ratings_record")
@ToString
public class ChessGameUsersRatingsRecord {
    @Id
    @Column(name = "game_id")
    private String gameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chess_game_type")
    private ChessGameTypeWithTimings chessGameType;

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


    public String getUsernameByUserColor(ChessColor userColor) {
        if (userColor == ChessColor.WHITE) return whiteUsername;
        if (userColor == ChessColor.BLACK) return blackUsername;
        throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT, "userColor", userColor));
    }

    public ChessColor getUserColorByUsername(String username) {
        if (username == null) return null;
        if (username.equals(whiteUsername)) return ChessColor.WHITE;
        if (username.equals(blackUsername)) return ChessColor.BLACK;
        return null;
    }

    public int getInitialRatingByUserColor(ChessColor userColor) {
        if (userColor == ChessColor.WHITE) return whiteInitialRating;
        if (userColor == ChessColor.BLACK) return blackInitialRating;
        throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT, "userColor", userColor));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGameUsersRatingsRecord record = (ChessGameUsersRatingsRecord) o;
        return finished == record.finished && whiteInitialRating == record.whiteInitialRating
                && blackInitialRating == record.blackInitialRating && Objects.equals(gameId, record.gameId)
                && chessGameType == record.chessGameType && Objects.equals(startedAt, record.startedAt)
                && Objects.equals(whiteUsername, record.whiteUsername) && Objects.equals(blackUsername,
                record.blackUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, chessGameType, startedAt, finished, whiteUsername, whiteInitialRating,
                blackUsername, blackInitialRating);
    }
}
