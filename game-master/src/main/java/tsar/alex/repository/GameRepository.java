package tsar.alex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import tsar.alex.model.Game;

import java.util.List;

public interface GameRepository extends MongoRepository<Game, String> {

    @Query(value = "{ 'finished' : false, '$or':[{'UsersInGame.whiteUsername': ?0}, {'UsersInGame.whiteUsername': ?0}] }")
    List<Game> findActiveGamesByUsername(String username);

    List<Game> getGamesByFinishedFalse();
}
