package tsar.alex.utils;

import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Constants.GAME_ID_REGEX;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class Utils {

    public static String getBindingResultErrorsAsString(BindingResult bindingResult) {
        StringBuilder errorMsg = new StringBuilder();

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            String message = error.getDefaultMessage();
            if (message != null && !message.isBlank()) {
                if (!errorMsg.isEmpty()) {
                    errorMsg.append(".\n");
                }
                errorMsg.append(message);
            }
        }

        return errorMsg.toString();
    }

    public static String getConstraintViolationsAsString(Set<ConstraintViolation<Object>> violations) {
        return String.join(". ", violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()));
    }

    public static String validateGameId(String gameId) {
        if (gameId == null) {
            return GAME_ID_NULL;
        }
        if (!Pattern.matches(GAME_ID_REGEX, gameId)) {
            return INCORRECT_GAME_ID;
        }
        return null;
    }

    public static String getCurrentUsername() {
        Jwt principal = (Jwt) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return principal.getClaim("username");
    }
}
