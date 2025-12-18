package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric snapshot write query object for controller layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshotWriteQO {

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
     * Metric write list.
     */
    @NotNull(message = "Metrics cannot be null")
    @Valid
    private List<EchoMetricQO> metrics;

    /**
     * Snapshot timestamp in milliseconds. If null, use current timestamp.
     */
    private Long snapshotTs;
}

