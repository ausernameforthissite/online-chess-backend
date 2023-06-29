package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
public class LoginRefreshOkDto implements LoginRefreshDto {
    private String accessToken;
    private RefreshTokenDto refreshTokenDto;
}