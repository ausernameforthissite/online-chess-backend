package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.User;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenValidationOkResult implements RefreshTokenValidationResult {
    private User user;
}