package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.RefreshTokenValidationBadResult;
import tsar.alex.dto.RefreshTokenValidationOkResult;
import tsar.alex.dto.RefreshTokenValidationResult;
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
    private long refreshTokenExpirationSeconds;

    public RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenValue(UUID.randomUUID().toString())
                .maxAge(refreshTokenExpirationSeconds)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpirationSeconds)).build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenValidationResult validateRefreshTokenAndRetrieveUser(RefreshToken refreshToken) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByTokenValue(
                refreshToken.getTokenValue());

        if (refreshTokenOptional.isEmpty()) {
            return new RefreshTokenValidationBadResult(REFRESH_TOKEN_NOT_FOUND);
        }

        RefreshToken persistentRefreshToken = refreshTokenOptional.get();
        Instant expiresAt = persistentRefreshToken.getExpiresAt();
        refreshTokenRepository.delete(persistentRefreshToken);

        if (expiresAt.isBefore(Instant.now())) {
            return new RefreshTokenValidationBadResult(EXPIRED);
        }

        return new RefreshTokenValidationOkResult(persistentRefreshToken.getUser());
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByTokenValue(refreshToken.getTokenValue());
    }

    @Scheduled(fixedDelay = 3600 * 1000, initialDelay = 3600 * 1000)
    public void deleteExpiredTokensFromDB() {
        Instant dateTime = Instant.now();
        refreshTokenRepository.deleteByExpiresAtBefore(dateTime);
    }

}