package tsar.alex.utils;

public class CommonTextConstants {
    public static final String NO_MATCH = "Матч с id=%s не найден.";
    public static final String NO_ACTIVE_MATCH = "Матч с id=%s не существует или уже завершён.";
    public static final String ALREADY_SEARCHING = "Вы уже ищите игру (возможно, из другой вкладки или с другого устройства).";
    public static final String ALREADY_IN_MATCH = "Вы уже в игре. matchId=%s";
    public static final String ALREADY_SUBSCRIBED = "Вы уже подключены к игре (возможно, из другой вкладки или с другого устройства).";
    public static final String INCORRECT_MATCH_ID = "Некорректный matchId.";

    public static final String INCORRECT_WEBSOCKET_CONTENT = "Incorrect websocket message content type. Expected content type is JSON.";
    public static final String INCORRECT_JSON = "Unable to parse incoming JSON. Incorrect incoming JSON object.";
    public static final String UNKNOWN_ERROR = "Unknown server error.";
    public static final String SEVERAL_ACTIVE_MATCHES = "Several active matches were found for user with username: %s";
    public static final String ALREADY_EXISTS = "Rating already exists for user with username: %s";
    public static final String MATCH_ID_NOT_FOUND = "No rating record was found for match with matchId: %s";
    public static final String MATCH_ID_NULL = "matchId must not be null.";
    public static final String NO_MATCH_ID_HEADER = "No Match-Id header.";
    public static final String NOT_FOUND_USERNAME = "No rating was found for user with username=%s.";
    public static final String NO_AUTHORIZATION_HEADER = "No X-Authorization header was found.";
    public static final String UNAUTHORIZED = "Unauthorized.";
    public static final String NO_ACTIVE_SESSION = "No active websocket session with sessionId=%s was found.";
    public static final String CANNOT_UNSUBSCRIBE = "User with username=%s and sessionId=%s tried to unsubscribe ChessMatch.";
    public static final String NOT_SUBSCRIBED = "You are not subscribed to match with matchId=%s.";
    public static final String MATCH_FINISHED = "The match is already finished.";
}
