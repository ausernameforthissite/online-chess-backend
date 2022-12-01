package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class AccessToken {
    private String token;
    private Instant expiresAt;
}
