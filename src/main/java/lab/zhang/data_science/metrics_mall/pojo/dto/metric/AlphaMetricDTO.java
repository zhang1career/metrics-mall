package lab.zhang.data_science.metrics_mall.pojo.dto.metric;

import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * The fundamental unit of metric data access object
 *
 * @author Rongjin Zhang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlphaMetricDTO {

    /**
     * Metric value.
     */
    private String value;

    /**
     * Snapshot timestamp in milliseconds.
     * 0 means return latest snapshot time, null means not return timestamp.
     */
    private Long snapshotTs;


    private SnapshotSourceTypeEnum sourceType;
}
