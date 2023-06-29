package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "rating_initialized")
    private boolean ratingInitialized;

    @OneToMany(mappedBy = "user")
    private List<RefreshToken> refreshToken;
}