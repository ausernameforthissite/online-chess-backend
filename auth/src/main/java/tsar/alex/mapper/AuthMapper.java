package tsar.alex.mapper;

import java.util.Collections;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tsar.alex.dto.*;
import tsar.alex.dto.request.InitializeUsersRatingsRequest;
import tsar.alex.dto.request.LoginRegisterRequest;
import tsar.alex.dto.response.LoginRefreshBadResponse;
import tsar.alex.dto.response.LoginRefreshOkResponse;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    User mapToUser(LoginRegisterRequest loginRegisterRequest);

    @Mapping(target = "tokenValue", source = "tokenValue")
    RefreshToken mapToRefreshToken(String tokenValue);

    @Mapping(target = "tokenValue", source = "tokenValue")
    @Mapping(target = "maxAgeSeconds", source = "maxAge")
    RefreshTokenDto mapToRefreshTokenDto(RefreshToken refreshToken);

    @Mapping(target = "message", source = "message")
    LoginRefreshBadResponse mapToLoginRefreshBadResponse(LoginRefreshBadDto loginRefreshBadDto);

    @Mapping(target = "accessToken", source = "accessToken")
    LoginRefreshOkResponse mapToLoginRefreshOkResponse(LoginRefreshOkDto loginRefreshOkDto);

    default InitializeUsersRatingsRequest mapToInitializeUsersRatingRequest(User user) {
        return new InitializeUsersRatingsRequest(Collections.singleton(user.getUsername()));
    }
}
