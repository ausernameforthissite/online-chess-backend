package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
public class LoginRefreshBadDto implements LoginRefreshDto {
    private String message;
}