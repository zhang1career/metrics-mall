package lab.zhang.data_science.metrics_mall.pojo.dto.metric;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Multifaceted Metric query item model for service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EchoMetricDTO {

    /**
     * Metric code.
     */
    private String code;

    /**
     * Metric version.
     */
    private Integer version;

    /**
     * Dimension conditions map.
     */
    private Map<String, TypedValue> dimensionMap;

    /**
     * Snapshot timestamp in milliseconds.
     * 0 means return latest snapshot time, null means not return timestamp.
     */
    private Long snapshotTs;
}
