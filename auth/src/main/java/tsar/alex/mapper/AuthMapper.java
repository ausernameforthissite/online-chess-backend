package tsar.alex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tsar.alex.dto.*;
import tsar.alex.model.AccessToken;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    User mapToUser(RegisterRequest registerRequest);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    User mapToUser(LoginRequest loginRequest);

    @Mapping(target = "token", source = "token")
    RefreshToken mapToRefreshToken(String token);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "maxAgeSeconds", source = "maxAge")
    RefreshTokenDto mapToRefreshTokenDto(RefreshToken refreshToken);

    @Mapping(target = "accessToken", source = "token")
    AccessTokenDto mapToAccessTokenDto(AccessToken accessToken);

    @Mapping(target = "accessTokenDto", source = "accessTokenDto")
    @Mapping(target = "refreshTokenDto", source = "refreshTokenDto")
    AuthResponse mapToAuthResponse(AccessTokenDto accessTokenDto, RefreshTokenDto refreshTokenDto);
}
