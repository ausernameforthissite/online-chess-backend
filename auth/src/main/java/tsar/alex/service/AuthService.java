package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.*;

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
import tsar.alex.dto.*;
import tsar.alex.dto.request.InitializeUserRatingRequest;
import tsar.alex.dto.response.InitializeUserRatingOkResponse;
import tsar.alex.dto.response.InitializeUserRatingResponse;
import tsar.alex.dto.response.RegisterBadResponse;
import tsar.alex.dto.response.RegisterOkResponse;
import tsar.alex.dto.response.RegisterResponse;
import tsar.alex.exception.RestApiResponseException;
import tsar.alex.mapper.AuthMapper;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;
import tsar.alex.repository.UserRepository;
import tsar.alex.security.JwtProvider;
import tsar.alex.utils.Endpoints;

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


    public RegisterResponse register(User user) {
        if (userRepository.existsById(user.getUsername())) {
            return new RegisterBadResponse(ALREADY_REGISTERED);
        }
        user.setCreatedAt(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User persistentUser = userRepository.save(user);

        HttpEntity<InitializeUserRatingRequest> httpRequest = new HttpEntity<>(
                authMapper.mapToInitializeRatingRequest(persistentUser));
        InitializeUserRatingResponse response = restTemplate.postForObject(Endpoints.INITIALIZE_USER_RATING,
                httpRequest, InitializeUserRatingResponse.class);

        if (response instanceof InitializeUserRatingOkResponse) {
            return new RegisterOkResponse();
        } else {
            throw new RestApiResponseException(INCORRECT_RESPONSE);
        }
    }

    public LoginRefreshDto login(User user) {
        String username = user.getUsername();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User persistentUser = userRepository.findById(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND, username)));
        return generateLoginRefreshResponse(persistentUser);
    }

    public LoginRefreshDto refreshToken(RefreshToken refreshToken) {
        User user = refreshTokenService.validateRefreshTokenAndRetrieveUser(refreshToken);
        return generateLoginRefreshResponse(user);
    }

    public LoginRefreshDto generateLoginRefreshResponse(User user) {
        String accessToken = jwtProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);
        RefreshTokenDto refreshTokenDto = authMapper.mapToRefreshTokenDto(refreshToken);

        return new LoginRefreshDto(accessToken, refreshTokenDto);
    }
}
