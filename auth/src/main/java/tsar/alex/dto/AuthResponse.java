package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private AccessTokenDto accessTokenDto;
    private RefreshTokenDto refreshTokenDto;
}

