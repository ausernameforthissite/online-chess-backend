package tsar.alex.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import tsar.alex.model.AccessToken;
import tsar.alex.model.User;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration-time.seconds}")
    private Long jwtExpirationSeconds;

    public AccessToken generateToken(User user) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("online-chess-auth")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtExpirationSeconds))
                .claim("username", user.getUsername())
                .claim("scope", "ROLE_USER")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new AccessToken(token);
    }

}