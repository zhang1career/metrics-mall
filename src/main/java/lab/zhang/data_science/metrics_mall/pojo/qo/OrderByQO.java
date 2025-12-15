package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order by query object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderByQO {

    /**
     * Field name.
     */
    @NotBlank(message = "Field cannot be blank")
    private String field;

    /**
     * Sort direction: asc, desc.
     */
    @NotBlank(message = "Sort cannot be blank")
    private String sort;
}
