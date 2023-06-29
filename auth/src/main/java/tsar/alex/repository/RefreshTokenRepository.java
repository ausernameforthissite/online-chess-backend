package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tsar.alex.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void deleteByTokenValue(String tokenValue);

    void deleteByExpiresAtBefore(Instant dateTime);

}