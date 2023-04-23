package tsar.alex.utils;

public class Constants {
    public static final long NOT_SUBSCRIBED_SESSION_TIMEOUT_MS = 10 * 1000;
    public static final long SESSION_TO_BE_CLOSED_TIMEOUT_MS = 5 * 1000;

    public static final int PLAYERS_PER_MATCH = 2;

    public static final String MATCH_ID_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int MATCH_ID_SIZE = 15;
    public static final String MATCH_ID_REGEX = "^[" + MATCH_ID_ALPHABET +"]{" + MATCH_ID_SIZE + "}$";
}
