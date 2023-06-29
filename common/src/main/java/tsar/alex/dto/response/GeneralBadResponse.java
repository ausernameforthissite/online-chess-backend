package tsar.alex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeneralBadResponse implements RestApiBadResponse {
    private String message;
}