package com.example.linkshortener.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullOrPositiveValidator implements ConstraintValidator<NullOrPositive, Number> {
    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || value.doubleValue() > 0L;
    }
}
