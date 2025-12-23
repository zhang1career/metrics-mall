package lab.zhang.data_science.metrics_mall.model.metric;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * The fundamental unit of metric data,
 * representing a single metric value at a specific point in time.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlphaMetric {

    /**
     * Metric value.
     */
    private TypedValue value;

    /**
     * Metric snapshot timestamp, in milliseconds.
     */
    private Long snapshotTs;

    /**
     * Source type of metric value
     */
    private SnapshotSourceTypeEnum sourceType;
}
