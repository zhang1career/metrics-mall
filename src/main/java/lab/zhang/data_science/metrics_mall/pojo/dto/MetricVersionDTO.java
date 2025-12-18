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
public class MetricVersionDTO {

    /**
     * Metric code.
     */
    private String metricCode;

    /**
     * Version number.
     */
    private Integer version;

    /**
     * Whether this version is the main version: 0=no, 1=yes.
     */
    private Integer isMain;
}
