package tsar.alex.utils;

import tsar.alex.model.PersonalMatchResultEnum;

public class EloRating {
    public static final int DEFAULT_USER_RATING = 1000;
    public static final int[] K_VALUES = {40, 20, 10};

    public static int getRatingChange(int rating1, int rating2, int K1, PersonalMatchResultEnum personalMatchResult) {
        double expectedScore = getExpectedScore(rating1, rating2);
        return Math.toIntExact(Math.round(K1 * (personalMatchResult.getScore() - expectedScore)));
    }

    private static double getExpectedScore(int rating1, int rating2) {
        return 1.0/(1.0 + Math.pow(10.0, (rating2 - rating1)/400.0));
    }

    public static int updateK(int initialK, int updatedRating, int matchesPlayed) {
        if (initialK == K_VALUES[0] && matchesPlayed == 30) {
            return K_VALUES[1];
        }

        if (initialK == K_VALUES[1] && updatedRating >= 2400) {
            return K_VALUES[2];
        }

        return initialK;
    }
}
