package lab.zhang.data_science.metrics_mall.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidMetricDimensionsValidator.class)
@Documented
public @interface ValidMetricDimensions {

    String message() default "Invalid metric dimensions";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
