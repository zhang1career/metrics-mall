package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * YGroup view object.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionGroupRelVO extends BaseVO {

    private Long id;

    private Long metricId;

    private String dimensionIds;
}

