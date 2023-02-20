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
    @Column(name = "username")
    private String username;

    @Column(name = "user_rating")
    private Short rating;
}
