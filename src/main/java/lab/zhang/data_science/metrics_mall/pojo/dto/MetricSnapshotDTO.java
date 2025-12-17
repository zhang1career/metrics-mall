package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric snapshot query model for service layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshotDTO {
    
    /**
     * Entity code.
     */
    private String entityCode;
    
    /**
     * Entity identifier.
     */
    private Long entityId;
    
    /**
     * Metric query list.
     */
    private List<EchoMetricDTO> metricList;
    
    /**
     * Snapshot timestamp in milliseconds.
     * 0 means return latest snapshot time, null means not return timestamp.
     */
    private Long snapshotTs;

    /**
     * Require atomic metrics or not. True means only atomic metrics are accepted.
     */
    private Boolean isAtomic;
}

