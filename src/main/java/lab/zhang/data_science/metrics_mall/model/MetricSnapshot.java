package lab.zhang.data_science.metrics_mall.model;

import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric snapshot result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshot {
    /**
     * Entity.
     */
    private Entity entity;

    /**
     * Metric values map, key is metric code, value is metric value.
     */
    private List<BetaMetric> metricList;

    /**
     * Snapshot timestamp in milliseconds.
     */
    private Long snapshotTs;
}
