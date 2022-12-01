package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.AuthResponse;
import tsar.alex.dto.LoginRequest;
import tsar.alex.dto.RefreshTokenRequest;
import tsar.alex.dto.RegisterRequest;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;
import tsar.alex.service.AuthService;
import tsar.alex.service.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);
        System.out.println(registerRequest);
        System.out.println(user);
        authService.register(user);

        return new ResponseEntity<>("User registration successful", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = modelMapper.map(loginRequest, User.class);
        AuthResponse authResponse = authService.login(user);

        return new ResponseEntity<>(authResponse, HttpStatus.ACCEPTED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> login(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = modelMapper.map(refreshTokenRequest, RefreshToken.class);
        AuthResponse authResponse = authService.refreshToken(refreshToken);

        return new ResponseEntity<>(authResponse, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = modelMapper.map(refreshTokenRequest, RefreshToken.class);
        refreshTokenService.deleteRefreshToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body("Refresh Token Deleted Successfully!!");
    }
}
