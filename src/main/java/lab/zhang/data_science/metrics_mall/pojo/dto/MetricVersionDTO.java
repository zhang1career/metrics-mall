package lab.zhang.data_science.metrics_mall.pojo.dto;

import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Metric and Dimension relation DTO.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MetricVersionDTO extends BaseDTO {

    /**
     * Metric version id.
     */
    private Long id;

    /**
     * Metric id.
     */
    private Long metricId;

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

    /**
     * Life status
     */
    private LifeStatusEnum lifeStatus;

    /**
     * Calculation logic.
     */
    private String calcLogic;
}
