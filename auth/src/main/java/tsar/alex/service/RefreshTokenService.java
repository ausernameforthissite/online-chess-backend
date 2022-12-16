package tsar.alex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.exception.RefreshTokenException;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;
import tsar.alex.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token.expiration-time.seconds}")
    private Long refreshTokenExpirationSeconds;

    public RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .maxAge(refreshTokenExpirationSeconds)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpirationSeconds))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public User validateRefreshTokenAndRetrieveUser(RefreshToken refreshToken) throws RefreshTokenException {
        RefreshToken persistentRefreshToken = refreshTokenRepository.findByToken(refreshToken.getToken())
                .orElseThrow(() -> new RefreshTokenException("Refresh token not exist or expired"));

        if (persistentRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token is expired");
        }

        refreshTokenRepository.delete(persistentRefreshToken);

        return persistentRefreshToken.getUser();
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken.getToken());
    }

    @Scheduled(fixedDelay = 90 * 1000, initialDelay = 90 * 1000)
    public void deleteExpiredTokensFromDB() {
        Instant dateTime = Instant.now();
        refreshTokenRepository.deleteByExpiresAtBefore(dateTime);
    }

}
