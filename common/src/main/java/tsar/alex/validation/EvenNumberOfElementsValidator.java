package tsar.alex.validation;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EvenNumberOfElementsValidator implements ConstraintValidator<EvenNumberOfElements, List<?>> {
    @Override
    public boolean isValid(List<?> values, ConstraintValidatorContext context) {
        return values.size() % 2 == 0;
    }
}