package lab.zhang.data_science.metrics_mall.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Metric snapshot response DTO for controller layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshotVO {

    /**
     * Entity code.
     */
    @JsonProperty("ec")
    private String entityCode;

    /**
     * Entity identifier.
     */
    @JsonProperty("eid")
    private Long entityId;

    /**
     * Metric values map, key is metric code, value is metric value.
     */
    @JsonProperty("values")
    private Map<String, TypedValue> valueMap;

    /**
     * Metric timestamps map, key is metric code, value is timestamp in milliseconds.
     * Only present when snapshotTs is 0 or specified.
     */
    @JsonProperty("_ts")
    private Map<String, Long> snapshotTsMap;
}

