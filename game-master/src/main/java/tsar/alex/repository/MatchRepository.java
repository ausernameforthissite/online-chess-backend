package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tsar.alex.model.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT COUNT(m) > 0 FROM match m WHERE m.isFinished = false AND (m.blackUserId = ?1 OR m.whiteUserId = ?1)")
    boolean isUserInMatch(Long userId);
}
