package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenValidationBadResult implements RefreshTokenValidationResult {
    private String message;
}