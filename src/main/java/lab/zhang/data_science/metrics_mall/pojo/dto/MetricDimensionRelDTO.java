package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric and Dimension relation DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionRelDTO {

    /**
     * Metric code.
     */
    private String metricCode;

    /**
     * Dimension code.
     */
    private String dimensionCode;

    /**
     * Is hot dimension.
     */
    private Integer isHot;

    /**
     * Validation rules in JSON format.
     */
    private String validation;
}
