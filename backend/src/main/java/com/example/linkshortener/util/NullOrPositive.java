package com.example.linkshortener.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = NullOrPositiveValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NullOrPositive {
    String message() default "must be null or positive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class NullOrPositiveValidator implements ConstraintValidator<NullOrNotBlank, Number> {
    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || value.doubleValue() > 0L;
    }
}