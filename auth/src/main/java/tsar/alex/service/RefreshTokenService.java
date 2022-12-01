package tsar.alex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

        if (refreshTokenRepository.existsByUser(user)) {
            refreshTokenRepository.deleteByUser(user);
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpirationSeconds))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public User validateRefreshTokenAndRetrieveUser(String token) throws RefreshTokenException {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not exist or expired"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token is expired");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);
        return user;
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken.getToken());
    }


}
