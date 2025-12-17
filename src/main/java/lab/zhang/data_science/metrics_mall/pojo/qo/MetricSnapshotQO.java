package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * Metric snapshot query object for controller layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshotQO {
    
    /**
     * Entity code.
     */
    @NotBlank(message = "Entity code cannot be blank")
    private String ec;
    
    /**
     * Entity identifier.
     */
    @NotNull(message = "Entity id cannot be null")
    private Long eid;
    
    /**
     * Metric query list.
     */
    @NotNull(message = "Metrics cannot be null")
    @Valid
    private List<EchoMetricQO> metrics;
    
    /**
     * Snapshot timestamp in milliseconds. 0 means return latest snapshot time, null means not return timestamp.
     */
    private Long snapshotTs;

    /**
     * Require atomic metrics or not. True means only atomic metrics are accepted.
     */
    private Integer isAtomic;

}

