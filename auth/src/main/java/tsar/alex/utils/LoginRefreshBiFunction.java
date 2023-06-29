package tsar.alex.utils;

import tsar.alex.dto.LoginRefreshDto;
import tsar.alex.model.RefreshToken;
import tsar.alex.model.User;

@FunctionalInterface
public interface LoginRefreshBiFunction {
    LoginRefreshDto doLoginRefreshOperation(User user, RefreshToken refreshToken);
}