package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric and Dimension relations DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionRelsDTO {

    /**
     * Metric code.
     */
    private String metricCode;

    /**
     * Dimension code.
     */
    private List<String> dimensionCodeList;
}
