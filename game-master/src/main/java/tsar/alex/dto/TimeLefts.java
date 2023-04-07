package tsar.alex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeLefts {
    private long newTimeLeft;
    private long enemyTimeLeft;
    private long thisMoveTime;
}
