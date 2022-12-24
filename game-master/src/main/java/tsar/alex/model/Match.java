package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "white_user_id")
    private Long whiteUserId;

    @Column(name = "black_user_id")
    private Long blackUserId;

    @Column(name = "is_finished")
    private Boolean isFinished;
}
