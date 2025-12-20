package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric Dimension Relation view object.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionRelVO {

    private Long metricMetaId;

    private Long dimensionId;

    private Integer isHot;

    private String validation;
}

