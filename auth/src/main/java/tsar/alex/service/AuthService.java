package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.api.client.AuthRestClient;
import tsar.alex.dto.*;
import tsar.alex.dto.response.RegisterBadResponse;
import tsar.alex.dto.response.RegisterOkResponse;
import tsar.alex.dto.response.RegisterResponse;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.mapper.AuthMapper;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;
import tsar.alex.repository.UserRepository;
import tsar.alex.utils.JwtProvider;

import java.time.Instant;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthRestClient authRestClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public RegisterResponse register(User user) {
        String username = user.getUsername();

        if (userRepository.existsById(username)) {
            log.debug(String.format(ALREADY_REGISTERED_LOG, username));
            return new RegisterBadResponse(ALREADY_REGISTERED);
        }

        user.setCreatedAt(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User persistentUser = userRepository.save(user);

        if (authRestClient.initializeUsersRatings(authMapper.mapToInitializeUsersRatingRequest(persistentUser))) {
            persistentUser.setRatingInitialized(true);
            log.debug(String.format(RATINGS_INITIALIZED_LOG, username));
        } else {
            log.debug(String.format(RATINGS_NOT_INITIALIZED_LOG, username));
        }

        return new RegisterOkResponse();
    }

    public LoginRefreshDto login(User user) {
        String username = user.getUsername();
        Optional<User> userOptional =  userRepository.findById(username);

        if (userOptional.isEmpty()) {
            log.debug(String.format(USER_NOT_FOUND, username));
            return new LoginRefreshBadDto(WRONG_LOGIN_OR_PASSWORD);
        }

        User persistentUser = userOptional.get();

        if (!passwordEncoder.matches(user.getPassword(), persistentUser.getPassword())) {
            log.debug(WRONG_PASSWORD);
            return new LoginRefreshBadDto(WRONG_LOGIN_OR_PASSWORD);
        }

        return generateLoginRefreshOkResponse(persistentUser);
    }

    public LoginRefreshDto refreshToken(RefreshToken refreshToken) {
        RefreshTokenValidationResult refreshTokenValidationResult = refreshTokenService.validateRefreshTokenAndRetrieveUser(
                refreshToken);

        if (refreshTokenValidationResult instanceof RefreshTokenValidationBadResult badResult) {
            String message = badResult.getMessage();
            log.debug(message);
            return new LoginRefreshBadDto(message);
        } else if (refreshTokenValidationResult instanceof RefreshTokenValidationOkResult okResult) {
            return generateLoginRefreshOkResponse(okResult.getUser());
        } else {
            throw new UnexpectedObjectClassException(String.format(UNEXPECTED_OBJECT_CLASS, "refreshTokenValidationResult",
                    refreshTokenValidationResult.getClass().getName()));
        }
    }

    private LoginRefreshOkDto generateLoginRefreshOkResponse(User user) {
        String accessToken = jwtProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);
        return new LoginRefreshOkDto(accessToken, authMapper.mapToRefreshTokenDto(refreshToken));
    }

    public void isUserRegisteredByUsername(String username) {
        User persistentUser = userRepository.findById(username).orElseThrow(
                () -> new DatabaseRecordNotFoundException(String.format(UNREGISTERED_USER_AUTHENTICATED, username)));

        if (persistentUser.isRatingInitialized()) {
            log.warn(String.format(RATINGS_STATUS_MISMATCH, username));
        } else {
            persistentUser.setRatingInitialized(true);
        }
    }
}