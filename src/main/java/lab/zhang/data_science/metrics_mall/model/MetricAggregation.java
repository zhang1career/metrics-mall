package lab.zhang.data_science.metrics_mall.model;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Metric aggregation result.
 */
@Data
@AllArgsConstructor
public class MetricAggregation {
    /**
     * Metric metadata map, key is metric code, value is metric metadata.
     */
    private Map<String, PrimeMetric> metricMap;

    /**
     * Metric values map, key is dimension map, value is metric code to metric value map.
     */
    private Map<Map<String, TypedValue>, Map<String, TypedValue>> dimensionValueMap;
}
