package lab.zhang.data_science.metrics_mall.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;


public class ValidMetricDimensionsValidator implements ConstraintValidator<ValidMetricDimensions, Object> {

    @Override
    public void initialize(ValidMetricDimensions constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull annotation
        if (value == null) {
            return true;
        }
        // Check if the value is a Map
        if (!(value instanceof Map<?, ?>)) {
            return false;
        }
        // Check if the dimensions is registered

        return true;
    }
}

