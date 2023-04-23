package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tsar.alex.model.ChessMatchUserRatingsRecord;

import java.util.List;

public interface ChessMatchUserRatingsRecordRepository extends JpaRepository<ChessMatchUserRatingsRecord, String> {

    @Query("SELECT r FROM ChessMatchUserRatingsRecord r WHERE r.finished = false AND (r.whiteUsername = ?1 OR r.blackUsername = ?1)")
    List<ChessMatchUserRatingsRecord> findRecordsOfActiveMatchesByUsername(String username);
}