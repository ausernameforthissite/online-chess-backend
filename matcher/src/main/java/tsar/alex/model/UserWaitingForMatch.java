package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tsar.alex.dto.FindMatchResult;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWaitingForMatch implements Comparable<UserWaitingForMatch> {

    private Thread thread;
    private UserRating userRating;
    private FindMatchResult result;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWaitingForMatch that = (UserWaitingForMatch) o;
        return Objects.equals(userRating.getUserId(), that.userRating.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userRating.getUserId());
    }

    @Override
    public int compareTo(UserWaitingForMatch o) {
        return Short.compare(this.getUserRating().getRating(), o.getUserRating().getRating());
    }
}
