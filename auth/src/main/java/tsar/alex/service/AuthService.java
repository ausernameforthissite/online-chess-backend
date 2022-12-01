package tsar.alex.service;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.dto.AuthResponse;
import tsar.alex.model.AccessToken;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;
import tsar.alex.repository.UserRepository;
import tsar.alex.security.JwtProvider;

import java.time.Instant;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public void register(User user) {
        user.setCreatedAt(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User persistentUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user with such username!"));
        return generateAuthResponseWithBothTokens(persistentUser);
    }

    public AuthResponse refreshToken(RefreshToken refreshToken) {
        User user = refreshTokenService.validateRefreshTokenAndRetrieveUser(
                refreshToken.getToken());
        return generateAuthResponseWithBothTokens(user);
    }

    public AuthResponse generateAuthResponseWithBothTokens(User user) {
        AccessToken accessToken = jwtProvider.generateTokenWithUsername(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken.getToken())
                .expiresAt(accessToken.getExpiresAt())
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
