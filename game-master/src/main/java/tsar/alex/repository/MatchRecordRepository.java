package tsar.alex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tsar.alex.model.MatchRecord;

public interface MatchRecordRepository extends MongoRepository<MatchRecord, Long> {

}
