package tsar.alex.api.controller;

import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Endpoints.*;

import javax.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tsar.alex.api.client.AuthRestClient;
import tsar.alex.dto.*;
import tsar.alex.dto.request.LoginRegisterRequest;
import tsar.alex.dto.request.CheckRegisteredRequest;
import tsar.alex.dto.response.*;
import tsar.alex.exception.DatabaseRecordNotFoundException;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.mapper.AuthMapper;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;
import tsar.alex.service.AuthService;
import tsar.alex.service.RefreshTokenService;
import tsar.alex.utils.LoginRefreshBiFunction;
import tsar.alex.utils.Utils;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final Validator validator;
    private final AuthRestClient authRestClient;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    @PostMapping(REGISTER)
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid LoginRegisterRequest loginRegisterRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errors = Utils.getBindingResultErrorsAsString(bindingResult);
            return new ResponseEntity<>(new RegisterBadResponse(errors), HttpStatus.BAD_REQUEST);
        }

        User user = authMapper.mapToUser(loginRegisterRequest);
        RegisterResponse registerResponse;
        HttpStatus httpStatus;

        try {
            registerResponse = authService.register(user);
            httpStatus = registerResponse instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error(e.toString());
            registerResponse = new RegisterBadResponse(SERVER_ERROR);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(registerResponse, httpStatus);
    }

    @PostMapping(LOGIN)
    public ResponseEntity<LoginRefreshResponse> login(@RequestBody @Valid LoginRegisterRequest loginRegisterRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errors = Utils.getBindingResultErrorsAsString(bindingResult);
            return new ResponseEntity<>(new LoginRefreshBadResponse(errors), HttpStatus.BAD_REQUEST);
        }

        return doLoginRefreshOperationAndPrepareResponse(
                (User user, RefreshToken refreshToken) -> authService.login(user),
                authMapper.mapToUser(loginRegisterRequest), null);
    }

    @PostMapping(REFRESH)
    public ResponseEntity<LoginRefreshResponse> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String token) {

        if (token == null || token.isBlank()) {
            return new ResponseEntity<>(new LoginRefreshBadResponse(REFRESH_TOKEN_BLANK), HttpStatus.BAD_REQUEST);
        }

        return doLoginRefreshOperationAndPrepareResponse(
                (User user, RefreshToken refreshToken) -> authService.refreshToken(refreshToken), null,
                authMapper.mapToRefreshToken(token));
    }

    private ResponseEntity<LoginRefreshResponse> doLoginRefreshOperationAndPrepareResponse(
            LoginRefreshBiFunction loginRefreshBiFunction, User user, RefreshToken refreshToken) {
        try {
            LoginRefreshDto loginRefreshDto = loginRefreshBiFunction.doLoginRefreshOperation(user, refreshToken);

            if (loginRefreshDto instanceof LoginRefreshOkDto loginRefreshOkDto) {
                return prepareLoginRefreshOkResponse(loginRefreshOkDto);
            } else if (loginRefreshDto instanceof LoginRefreshBadDto loginRefreshBadDto) {
                return new ResponseEntity<>(authMapper.mapToLoginRefreshBadResponse(loginRefreshBadDto),
                        HttpStatus.BAD_REQUEST);
            } else {
                throw new UnexpectedObjectClassException(String.format(UNEXPECTED_OBJECT_CLASS, "loginRefreshDto",
                        loginRefreshDto.getClass().getName()));
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error(e.toString());
            return new ResponseEntity<>(new LoginRefreshBadResponse(SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<LoginRefreshResponse> prepareLoginRefreshOkResponse(LoginRefreshOkDto loginRefreshOkDto) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        prepareRefreshTokenCookieAsString(loginRefreshOkDto.getRefreshTokenDto()))
                .body(authMapper.mapToLoginRefreshOkResponse(loginRefreshOkDto));
    }

    private String prepareRefreshTokenCookieAsString(RefreshTokenDto refreshTokenDto) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshTokenDto.getTokenValue()).httpOnly(true).path("/")
                .maxAge(refreshTokenDto.getMaxAgeSeconds()).build().toString();
    }

    @DeleteMapping(LOGOUT)
    public ResponseEntity<LogoutResponse> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String token) {

        if (token == null || token.isBlank()) {
            return new ResponseEntity<>(new LogoutBadResponse(REFRESH_TOKEN_BLANK), HttpStatus.BAD_REQUEST);
        }

        refreshTokenService.deleteRefreshToken(authMapper.mapToRefreshToken(token));
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("", 0L);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, prepareRefreshTokenCookieAsString(refreshTokenDto))
                .build();
    }


    @PostMapping(MATCHER_AVAILABLE)
    public ResponseEntity<Void> matcherMicroserviceBecameAvailable() {
        authRestClient.setMatcherMicroserviceAvailable(true);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(CHECK_REGISTERED)
    public ResponseEntity<CheckRegisteredResponse> checkIfUserIsRegisteredAndUpdateRatingsStatus(
            @RequestBody @Valid CheckRegisteredRequest checkRegisteredRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String message = String.format(OBJECT_HAS_ERRORS, "checkRegisteredRequest",
                    Utils.getBindingResultErrorsAsString(bindingResult));
            return new ResponseEntity<>(new CheckRegisteredBadResponse(message), HttpStatus.BAD_REQUEST);
        }

        try {
            authService.isUserRegisteredByUsername(checkRegisteredRequest.getUsername());
            return new ResponseEntity<>(new CheckRegisteredOkResponse(), HttpStatus.OK);
        } catch (DatabaseRecordNotFoundException e) {
            return new ResponseEntity<>(new CheckRegisteredBadResponse(e.toString()), HttpStatus.BAD_REQUEST);
        } finally {
            authRestClient.setMatcherMicroserviceAvailable(true);
        }
    }
}