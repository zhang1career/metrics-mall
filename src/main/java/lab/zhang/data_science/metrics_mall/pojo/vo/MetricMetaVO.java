package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric metadata DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricMetaVO {

    /**
     * Metric name in Chinese.
     */
    private String name;

    /**
     * Unit of the metric.
     */
    private String unit;

    /**
     * Precision for decimal display.
     */
    private Integer precision;
}
