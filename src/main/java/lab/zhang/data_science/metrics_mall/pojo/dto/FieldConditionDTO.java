package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldConditionDTO {

    /**
     * Field name.
     */
    private String field;

    /**
     * Operator: =, !=, >, <, >=, <=, in, not_in.
     */
    private String op;

    /**
     * Filter value.
     */
    private Object value;
}
