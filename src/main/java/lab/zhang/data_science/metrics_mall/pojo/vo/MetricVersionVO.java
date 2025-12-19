package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Metric version view object.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetricVersionVO extends BaseVO {

    private Long id;

    private Long metricId;

    private Integer version;

    private Integer isMain;

    private Integer lifeStatus;

    private String calcLogic;

    private String description;
}

