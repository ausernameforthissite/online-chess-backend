package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tsar.alex.model.CurrentUserRating;
import tsar.alex.model.CurrentUserRatingId;

@Repository
public interface CurrentUserRatingRepository extends JpaRepository<CurrentUserRating, CurrentUserRatingId> {
    boolean existsByUsername(String username);
}
