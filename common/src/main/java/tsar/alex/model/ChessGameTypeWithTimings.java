package tsar.alex.model;

import static tsar.alex.model.ChessGameType.*;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum ChessGameTypeWithTimings {
    BULLET_1_0(ChessGameType.BULLET, "bullet_1_0", 1 * 60 * 1000, 0),
    BULLET_1_2(ChessGameType.BULLET, "bullet_1_2", 1 * 60 * 1000, 2 * 1000),
    BLITZ_3_0(BLITZ, "blitz_3_0", 3 * 60 * 1000, 0),
    BLITZ_3_2(BLITZ, "blitz_3_2", 3 * 60 * 1000, 2 * 1000),
    RAPID_10_0(ChessGameType.RAPID, "rapid_10_0", 10 * 60 * 1000, 0),
    RAPID_10_5(ChessGameType.RAPID, "rapid_10_5", 10 * 60 * 1000, 5 * 1000),
    CLASSIC_30_0(ChessGameType.CLASSIC, "classic_30_0", 30 * 60 * 1000, 0),
    CLASSIC_30_30(ChessGameType.CLASSIC, "classic_30_30", 30 * 60 * 1000, 30 * 1000);


    private ChessGameType generalGameType;
    private String fullName;
    private long initialTimeMS;
    private long timeIncrementMS;

    public ChessGameType getGeneralGameType() {
        return generalGameType;
    }

    @JsonValue
    public String getFullName() {
        return fullName;
    }

    public long getInitialTimeMS() {
        return initialTimeMS;
    }

    public long getTimeIncrementMS() {
        return timeIncrementMS;
    }

}