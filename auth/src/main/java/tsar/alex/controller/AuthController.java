package tsar.alex.controller;

import static tsar.alex.utils.CommonTextConstants.*;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.*;
import tsar.alex.dto.request.LoginRegisterRequest;
import tsar.alex.dto.response.*;
import tsar.alex.mapper.AuthMapper;
import tsar.alex.model.User;
import tsar.alex.service.AuthService;
import tsar.alex.service.RefreshTokenService;
import tsar.alex.utils.Utils;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid LoginRegisterRequest loginRegisterRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new RegisterBadResponse(Utils.getBindingResultErrorsAsString(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }

        User user = authMapper.mapToUser(loginRegisterRequest);
        RegisterResponse response = authService.register(user);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRefreshResponse> login(@RequestBody @Valid LoginRegisterRequest loginRegisterRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new LoginRefreshBadResponse(Utils.getBindingResultErrorsAsString(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }

        User user = authMapper.mapToUser(loginRegisterRequest);
        LoginRefreshDto loginRefreshDto = authService.login(user);
        return generateRefreshTokenResponse(loginRefreshDto);
    }

    @GetMapping("/refresh")
    public ResponseEntity<LoginRefreshResponse> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String token) {
        if (token == null || token.isBlank()) {
            return new ResponseEntity<>(new LoginRefreshBadResponse(REFRESH_TOKEN_BLANK), HttpStatus.BAD_REQUEST);
        }

        LoginRefreshDto loginRefreshDto = authService.refreshToken(authMapper.mapToRefreshToken(token));
        return generateRefreshTokenResponse(loginRefreshDto);
    }

    public ResponseEntity<LoginRefreshResponse> generateRefreshTokenResponse(LoginRefreshDto loginRefreshDto) {
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE,
                        generateRefreshTokenCookieAsString(loginRefreshDto.getRefreshTokenDto()))
                .body(new LoginRefreshOkResponse(loginRefreshDto.getAccessToken()));
    }

    public String generateRefreshTokenCookieAsString(RefreshTokenDto refreshTokenDto) {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshTokenDto.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenDto.getMaxAgeSeconds())
                .build().toString();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String token) {
        if (token == null || token.isBlank()) {
            return new ResponseEntity<>(new LogoutBadResponse(REFRESH_TOKEN_BLANK), HttpStatus.BAD_REQUEST);
        }

        refreshTokenService.deleteRefreshToken(authMapper.mapToRefreshToken(token));
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("", 0L);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieAsString(refreshTokenDto)).build();
    }
}