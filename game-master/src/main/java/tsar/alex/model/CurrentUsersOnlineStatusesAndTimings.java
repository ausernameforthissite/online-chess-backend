package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUsersOnlineStatusesAndTimings {
    private boolean whiteUserOnline;
    private long whiteTimeLeftMS;
    private long whiteReconnectTimeLeftMS;
    private boolean blackUserOnline;
    private long blackTimeLeftMS;
    private long blackReconnectTimeLeftMS;
}