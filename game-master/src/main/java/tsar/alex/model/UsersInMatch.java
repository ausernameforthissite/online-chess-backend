package tsar.alex.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

public class UsersInMatch {

    private String[] usernames = new String[2];
    private ChessColor currentTurnUserColor;

    public UsersInMatch() {
    }

    public String[] getUsernames() {
        return usernames;
    }

    public void setUsernames(String[] usernames) {
        this.usernames = usernames;
    }

    public UsersInMatch(String whiteUsername, String blackUsername) {
        usernames[0] = whiteUsername;
        usernames[1] = blackUsername;
    }

    public UsersInMatch(String whiteUsername, String blackUsername, ChessColor currentTurnUserColor) {
        this(whiteUsername, blackUsername);
        this.currentTurnUserColor = currentTurnUserColor;
    }

    public String getUsernameByColor(ChessColor chessColor) {
        if (chessColor == ChessColor.WHITE) {
            return usernames[0];
        } else if (chessColor == ChessColor.BLACK) {
            return usernames[1];
        } else {
            return null;
        }
    }

    public ChessColor getColorByUsername(String username) {
        if (username == null) {
            return null;
        }
        if (username.equals(usernames[0])) {
            return ChessColor.WHITE;
        } else if (username.equals(usernames[1])) {
            return ChessColor.BLACK;
        } else {
            return null;
        }
    }

    public void setWhiteUsername(String whiteUsername) {
        usernames[0] = whiteUsername;
    }

    public void setBlackUserId(String blackUsername) {
        usernames[1] = blackUsername;
    }

    public ChessColor getCurrentTurnUserColor() {
        return currentTurnUserColor;
    }

    public void setCurrentTurnUserColor(ChessColor currentTurnUserColor) {
        this.currentTurnUserColor = currentTurnUserColor;
    }

    @JsonIgnore
    public String getCurrentTurnUsername() {
        if (currentTurnUserColor == ChessColor.WHITE) {
            return usernames[0];
        } else if (currentTurnUserColor == ChessColor.BLACK) {
            return usernames[1];
        } else {
            return null;
        }
    }

    @JsonIgnore
    public void setCurrentTurnUsername(String username) {
        if (username == null) {
            return;
        }

        if (username.equals(usernames[0])) {
            currentTurnUserColor = ChessColor.WHITE;
        } else if (username.equals(usernames[1])) {
            currentTurnUserColor = ChessColor.BLACK;
        } else {
            throw new RuntimeException();
        }
    }

    public boolean isCurrentTurn(String username) {
        return username != null && username.equals(getCurrentTurnUsername());
    }

    public boolean isCurrentTurn(ChessColor userColor) {
        return userColor == currentTurnUserColor;
    }

}
