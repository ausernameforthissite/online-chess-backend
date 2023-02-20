package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.*;
import tsar.alex.mapper.AuthMapper;
import tsar.alex.model.User;
import tsar.alex.service.AuthService;
import tsar.alex.service.RefreshTokenService;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AuthRequest authRequest) {
        User user = authMapper.mapToUser(authRequest);
        authService.register(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenDto> login(@RequestBody AuthRequest authRequest) {
        User user = authMapper.mapToUser(authRequest);
        AuthResponse authResponse = authService.login(user);
        return generateRefreshTokenResponse(authResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<AccessTokenDto> login(@CookieValue(name = "refresh-token") String token) {
        AuthResponse authResponse = authService.refreshToken(authMapper.mapToRefreshToken(token));

        return generateRefreshTokenResponse(authResponse);
    }

    public ResponseEntity<AccessTokenDto> generateRefreshTokenResponse(AuthResponse authResponse) {
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieAsString(authResponse.getRefreshTokenDto()))
                .body(authResponse.getAccessTokenDto());
    }

    public String generateRefreshTokenCookieAsString(RefreshTokenDto refreshTokenDto) {
        return   ResponseCookie
                .from("refresh-token", refreshTokenDto.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenDto.getMaxAgeSeconds())
                .build().toString();
    }


    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh-token") String token) {
        refreshTokenService.deleteRefreshToken(authMapper.mapToRefreshToken(token));

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("", 0L);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieAsString(refreshTokenDto)).build();
    }
}
