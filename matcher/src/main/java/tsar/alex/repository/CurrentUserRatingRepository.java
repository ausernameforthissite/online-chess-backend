package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tsar.alex.model.CurrentUserRating;

@Repository
public interface CurrentUserRatingRepository extends JpaRepository<CurrentUserRating, String> {
    boolean existsByUsername(String username);
}
