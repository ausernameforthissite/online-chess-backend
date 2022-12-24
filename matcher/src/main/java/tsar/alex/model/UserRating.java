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
@Entity(name = "user_rating")
public class UserRating {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_rating")
    private Short rating;
}
