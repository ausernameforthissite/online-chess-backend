package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tsar.alex.model.ChessGameUserRatingsRecord;

import java.util.List;

public interface ChessGameUserRatingsRecordRepository extends JpaRepository<ChessGameUserRatingsRecord, String> {

    @Query("SELECT r FROM ChessGameUserRatingsRecord r WHERE r.finished = false AND (r.whiteUsername = ?1 OR r.blackUsername = ?1)")
    List<ChessGameUserRatingsRecord> findRecordsOfActiveGamesByUsername(String username);
}