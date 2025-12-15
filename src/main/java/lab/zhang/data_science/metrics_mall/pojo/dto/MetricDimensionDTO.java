package lab.zhang.data_science.metrics_mall.pojo.dto;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Metric query item model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionDTO {

    /**
     * Metric code.
     */
    private String code;

    /**
     * Dimension conditions map.
     */
    private Map<String, TypedValue> dimensionMap;
}
