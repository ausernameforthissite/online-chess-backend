package tsar.alex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import tsar.alex.model.Match;

import java.util.Optional;

public interface MatchRepository extends MongoRepository<Match, Long> {

    @Query(value = "{ 'Finished' : false, 'usersInMatch.usernames': ?0 }", exists = true)
    boolean isUserInMatch(String username);

    @Query(value = "{ 'Finished' : false }", count = true)
    int testMethod(String username);

    @Override
    Optional<Match> findById(Long matchId);
}
