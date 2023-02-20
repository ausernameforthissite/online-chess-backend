package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tsar.alex.model.UserRating;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, String> {

    boolean existsByUsername(String username);
}
