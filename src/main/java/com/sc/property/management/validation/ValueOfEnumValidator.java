package com.sc.property.management.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, String> {
  private Class<? extends Enum<?>> enumClass;

  @Override
  public void initialize(ValueOfEnum annotation) {
    this.enumClass = annotation.enumClass();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Validation for @NotBlank or @NotNull should handle null.
    }

    return Arrays.stream(enumClass.getEnumConstants())
        .map(Enum::name)
        .anyMatch(enumValue -> enumValue.equalsIgnoreCase(value));
  }
}
