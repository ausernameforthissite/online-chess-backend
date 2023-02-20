package tsar.alex.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tsar.alex.dto.AccessTokenDto;
import tsar.alex.dto.AuthResponse;
import tsar.alex.dto.InitializeUserRatingRequest;
import tsar.alex.dto.RefreshTokenDto;
import tsar.alex.mapper.AuthMapper;
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

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public void register(User user) {
        user.setCreatedAt(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User persistentUser = userRepository.save(user);

        HttpEntity<InitializeUserRatingRequest> httpRequest = new HttpEntity<>(authMapper.mapToInitializeRatingRequest(persistentUser));
        HttpEntity<Void> response = restTemplate.postForEntity("http://localhost:8081/api/initialize_user_rating", httpRequest, Void.class);
    }

    public AuthResponse login(User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User persistentUser = userRepository.findById(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user with such username!"));
        return generateAuthResponse(persistentUser);
    }

    public AuthResponse refreshToken(RefreshToken refreshToken) {
        User user = refreshTokenService.validateRefreshTokenAndRetrieveUser(
                refreshToken);
        return generateAuthResponse(user);
    }


    public AuthResponse generateAuthResponse(User user) {
        AccessToken accessToken = jwtProvider.generateToken(user);
        AccessTokenDto accessTokenDto = authMapper.mapToAccessTokenDto(accessToken);
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);
        RefreshTokenDto refreshTokenDto = authMapper.mapToRefreshTokenDto(refreshToken);

        return authMapper.mapToAuthResponse(accessTokenDto, refreshTokenDto);
    }
}
