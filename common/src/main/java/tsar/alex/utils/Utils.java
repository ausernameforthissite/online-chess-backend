package tsar.alex.utils;

import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Constants.GAME_ID_REGEX;

import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolation;
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
                    errorMsg.append("\n");
                }
                errorMsg.append(message);
            }
        }

        return errorMsg.toString();
    }

    public static <T> String getConstraintViolationsAsString(Set<ConstraintViolation<T>> violations) {
        StringBuilder errorMsg = new StringBuilder();
        for (ConstraintViolation<T> constraintViolation : violations) {
            String message = constraintViolation.getMessage();
            if (message != null && !message.isBlank()) {
                if (!errorMsg.isEmpty()) {
                    errorMsg.append("\n");
                }
                errorMsg.append(message);
            }
        }
        return errorMsg.toString();
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
}
