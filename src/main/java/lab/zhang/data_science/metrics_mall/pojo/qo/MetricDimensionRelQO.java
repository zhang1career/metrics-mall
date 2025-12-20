package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric Dimension Relation query object for create operation.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionRelQO {

    @NotBlank(message = "Metric code cannot be blank")
    private String metricCode;

    @NotBlank(message = "Dimension code cannot be blank")
    private String dimensionCode;

    @NotNull(message = "Is hot cannot be null")
    private Integer isHot;

    private String validation;
}

