package lab.zhang.data_science.metrics_mall.pojo.qo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric version query object.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricVersionQO {

    private Long metricId;

    private Integer version;

    private Integer isMain;

    private Integer lifeStatus;
}

