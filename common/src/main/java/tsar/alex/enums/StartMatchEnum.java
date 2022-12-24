package tsar.alex.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StartMatchEnum {
    ERROR(false, "You are already in match!"),
    RETRY(false, "Something went wrong"),
    OK(true, "Match started successfully");

    private final Boolean result;
    private final String message;
}
