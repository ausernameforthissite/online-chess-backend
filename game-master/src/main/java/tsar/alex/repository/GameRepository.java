package tsar.alex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import tsar.alex.model.Game;

import java.util.List;

public interface GameRepository extends MongoRepository<Game, String> {

    @Query(value = "{ 'finished' : false, '$or':[{'UsersInGame.whiteUsername': {$in : ?0}}, {'UsersInGame.blackUsername': {$in : ?0}}] }",
            fields = "{ 'gameType' : 1, 'startedAt' : 1, 'usersInGame': 1 }")
    List<Game> findActiveGamesByUsernames(List<String> usernames);

    List<Game> getGamesByFinishedFalse();

}
