package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "current_user_rating")
public class CurrentUserRating {

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "rating")
    private int rating;

    @Column(name = "matches_played")
    private int matchesPlayed;

    @Column(name = "k")
    private int K;


    public int incrementMatchesPlayed() {
        return ++this.matchesPlayed;
    }

}
