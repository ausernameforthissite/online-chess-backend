package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tsar.alex.model.ChessGameUsersRatingsRecord;

import java.util.List;

public interface ChessGameUserRatingsRecordRepository extends JpaRepository<ChessGameUsersRatingsRecord, String> {

    @Query("SELECT r FROM ChessGameUsersRatingsRecord r WHERE r.finished = false AND (r.whiteUsername = ?1 OR r.blackUsername = ?1)")
    List<ChessGameUsersRatingsRecord> findRecordsOfActiveGamesByUsername(String username);

    List<ChessGameUsersRatingsRecord> findAllByGameIdIn(List<String> gameIds);
}
