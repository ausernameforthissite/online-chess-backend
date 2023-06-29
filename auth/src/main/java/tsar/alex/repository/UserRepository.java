package tsar.alex.repository;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tsar.alex.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "SELECT username FROM User u WHERE u.ratingInitialized = false")
    Set<String> getUsernamesByRatingInitializedFalse();

    @Modifying
    @Query(value = "UPDATE User u SET u.ratingInitialized = true WHERE u.username IN :usernames")
    void setRatingsInitializedByUsernames(Set<String> usernames);

}