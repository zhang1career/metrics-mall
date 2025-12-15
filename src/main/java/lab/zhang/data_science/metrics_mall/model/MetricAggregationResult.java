package lab.zhang.data_science.metrics_mall.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Metric aggregation result.
 */
@Data
@AllArgsConstructor
public class MetricAggregationResult {
    /**
     * Metric metadata map, key is metric code, value is metric metadata.
     */
    private final Map<String, MetricMeta> meta;

    /**
     * Aggregation result rows.
     */
    private final List<AggregationRow> rows;


    /**
     * Metric metadata.
     */
    @Getter
    @AllArgsConstructor
    public static class MetricMeta {
        private final String name;
        private final String unit;
        private final Integer precision;
    }

    /**
     * Aggregation row.
     */
    @Getter
    @AllArgsConstructor
    public static class AggregationRow {
        /**
         * Bucket time, format: "yyyy-MM-dd HH:mm:ss".
         */
        private final String bucketTime;

        /**
         * Dimension values map, key is dimension code, value is dimension value.
         */
        private final Map<String, String> dimMap;

        /**
         * Metric values map, key is metric code, value is metric value.
         */
        private final Map<String, Double> metricMap;
    }
}
