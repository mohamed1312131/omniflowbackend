package com.example.track.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HexColorValidator.class)
@Documented
public @interface HexColor {
    String message() default "Color must be a valid hex format (e.g., #3B82F6)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
