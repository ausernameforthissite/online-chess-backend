package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChessPositionStatusEnum {
    OK(""),
    THREEFOLD_REPETITION("Трёхкратное повторение позиции"),
    FIFTY_MOVE_NOT_CHANGE("50 ходов без необратимых изменений в положении фигур"),
    INSUFFICIENT_MATERIAL("Недостаточно материала для победы");

    private final String message;
}