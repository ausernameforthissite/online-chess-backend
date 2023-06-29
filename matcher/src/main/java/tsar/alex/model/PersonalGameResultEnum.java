package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PersonalGameResultEnum {
    WIN(1.0),
    DRAW(0.5),
    LOSS(0.0);

    private final double score;
}
