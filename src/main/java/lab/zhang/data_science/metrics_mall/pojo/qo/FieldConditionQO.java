package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter query object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldConditionQO {

    /**
     * Field name.
     */
    @NotBlank(message = "Field cannot be blank")
    private String field;

    /**
     * Operator: =, !=, >, <, >=, <=, in, not_in.
     */
    @NotBlank(message = "Operator cannot be blank")
    private String op;

    /**
     * Filter value.
     */
    @NotNull(message = "Value cannot be null")
    private Object value;
}
