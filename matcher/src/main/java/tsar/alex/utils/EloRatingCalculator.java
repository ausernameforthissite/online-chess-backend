package tsar.alex.utils;

import tsar.alex.model.PersonalGameResultEnum;

public class EloRatingCalculator {

    public static final int DEFAULT_USER_RATING = 1000;
    public static final int[] K_VALUES = {40, 20, 10};

    public static int calculateRatingChange(int rating1, int rating2, int K1,
            PersonalGameResultEnum personalGameResult) {
        double expectedScore = calculateExpectedScore(rating1, rating2);
        return Math.toIntExact(Math.round(K1 * (personalGameResult.getScore() - expectedScore)));
    }

    private static double calculateExpectedScore(int rating1, int rating2) {
        return 1.0 / (1.0 + Math.pow(10.0, (rating2 - rating1) / 400.0));
    }

    public static int updateK(int initialK, int updatedRating, int gamesPlayed) {
        if (initialK == K_VALUES[0] && gamesPlayed == 30) return K_VALUES[1];
        if (initialK == K_VALUES[1] && updatedRating >= 2400) return K_VALUES[2];
        return initialK;
    }
}
