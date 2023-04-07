package tsar.alex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import tsar.alex.model.Match;

import java.util.List;

public interface MatchRepository extends MongoRepository<Match, Long> {

    @Query(value = "{ 'finished' : false, '$or':[{'UsersInMatch.whiteUsername': ?0}, {'UsersInMatch.whiteUsername': ?0}] }")
    List<Match> findActiveMatchesByUsername(String username);

//    @Override
//    Optional<Match> findById(long matchId);


    List<Match> getMatchesByFinishedFalse();
}
