package tsar.alex.utils;

public class CommonTextConstants {
    public static final String ALREADY_REGISTERED = "Пользователь с таким именем уже зарегистрирован!";
    public static final String USER_NOT_FOUND = "There is no registered user with username: %s";
    public static final String INCORRECT_RESPONSE = "Received response of incorrect type from matcher microservice";
    public static final String NO_GAME = "Игра с gameId=%s не найдена.";
    public static final String NO_ACTIVE_GAME = "Игра с gameId=%s не существует или уже завершена.";
    public static final String ALREADY_SEARCHING = "Вы уже ищите игру (возможно, из другой вкладки или с другого устройства).";
    public static final String ALREADY_IN_GAME = "Вы уже в игре с gameId=%s";
    public static final String ALREADY_SUBSCRIBED = "Вы уже подключены к игре (возможно, из другой вкладки или с другого устройства).";
    public static final String INCORRECT_GAME_ID = "Некорректный gameId.";

    public static final String NOT_FOUND = "Refresh token not exist or expired";
    public static final String EXPIRED = "Refresh token is expired";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    public static final String REFRESH_TOKEN_BLANK = "refresh-token cookie is blank";
    public static final String INCORRECT_WEBSOCKET_CONTENT = "Incorrect websocket message content type. Expected content type is JSON.";
    public static final String INCORRECT_JSON = "Unable to parse incoming JSON. Incorrect incoming JSON object.";
    public static final String UNKNOWN_ERROR = "Unknown server error.";
    public static final String SEVERAL_ACTIVE_GAMES = "Several active games were found for user with username: %s.";
    public static final String ALREADY_EXISTS = "Rating already exists for user with username: %s.";
    public static final String GAME_BEING_CREATED = "Can't cancel. Game is already being created.";
    public static final String GAME_ID_NOT_FOUND = "No rating record was found for match with gameId: %s.";
    public static final String GAME_ID_NULL = "gameId must not be null.";
    public static final String NO_GAME_ID_HEADER = "No Game-Id header.";
    public static final String DB_AND_OBJECT_NOT_CORRESPOND_ID = "game DB contains game with gameId = %s, but chessGameWebsocketRoomsHolder doesn't.";
    public static final String DB_AND_OBJECT_NOT_CORRESPOND_FINISHED = "game with gameId = %s has status \"finished\" in game DB, but not in chessGameWebsocketRoomsHolder.";
    public static final String NOT_YOUR_TURN = "It's not your turn.";
    public static final String CHESS_MOVE_NULL = "chessMove is null.";
    public static final String BAD_MOVE = "Bad move.";
    public static final String NO_USER_RATING = "No rating was found for username = %s and chessGameType = %s.";
    public static final String NO_AUTHORIZATION_HEADER = "No X-Authorization header was found.";
    public static final String NO_GAME_TYPE_HEADER = "No GameType header was found.";
    public static final String UNAUTHORIZED = "Unauthorized.";
    public static final String INCORRECT_CHESS_PIECE_CLASS = "Incorrect chessPieceClass = %s.";
    public static final String INCORRECT_GAME_TYPE = "Incorrect ChessGameType value.";
    public static final String NO_ACTIVE_SESSION = "No active websocket session with sessionId=%s was found.";
    public static final String CANNOT_UNSUBSCRIBE = "User with username=%s and sessionId = %s tried to unsubscribe ChessGame websocket.";
    public static final String NOT_SUBSCRIBED = "You are not subscribed to game with gameId = %s.";
    public static final String GAME_FINISHED = "The game is already finished.";
}
