package tsar.alex.utils;

public final class Endpoints {

    // Auth endpoints
    public static final String AUTH_BASE_URL = "http://localhost:8080/api/auth";
    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String REFRESH = "/refresh";
    public static final String LOGOUT = "/logout";
    public static final String CHECK_REGISTERED = "/check_registered";

    // Matcher endpoints
    public static final String MATCHER_BASE_URL = "http://localhost:8081/api";
    public static final String USERS_RATINGS = "/game/{id}/ratings";
    public static final String USER_STATUS = "/user";
    public static final String INITIALIZE_USERS_RATINGS = "/initialize_users_ratings";
    public static final String UPDATE_USERS_RATINGS = "/update_users_ratings";
    public static final String AUTH_AVAILABLE = "/auth";
    public static final String GAME_MASTER_AVAILABLE = "/game_master";


    // Game Master endpoints
    public static final String GAME_MASTER_BASE_URL = "http://localhost:8082/api/game";
    public static final String START_GAMES = "/start";
    public static final String GAME_STATE = "/{id}/state";

    // Common endpoints
    public static final String MATCHER_AVAILABLE = "/matcher";
}
