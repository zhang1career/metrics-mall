package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Aggregation row DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationRowVO {

    /**
     * Bucket time, format: "yyyy-MM-dd HH:mm:ss".
     */
    private String bucketTime;

    /**
     * Dimension values map, key is dimension code, value is dimension value.
     */
    private Map<String, String> dims;

    /**
     * Metric values map, key is metric code, value is metric value.
     */
    private Map<String, Double> metrics;
}
