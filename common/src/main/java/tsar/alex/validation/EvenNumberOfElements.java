package tsar.alex.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = EvenNumberOfElementsValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvenNumberOfElements {
    String message() default "Number of elements is not even.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}