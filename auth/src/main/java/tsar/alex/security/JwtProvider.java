package tsar.alex.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import tsar.alex.model.AccessToken;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration-time.seconds}")
    private Long jwtExpirationSeconds;

    public AccessToken generateTokenWithUsername(String username) {

        Instant expiresAt = Instant.now().plusSeconds(jwtExpirationSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("online-chess-dispatcher")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtExpirationSeconds))
                .subject(username)
                .claim("scope", "ROLE_USER")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new AccessToken(token, expiresAt);
    }

}