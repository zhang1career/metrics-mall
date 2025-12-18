package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric snapshot write model for service layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshotWriteDTO {

    /**
     * Entity code.
     */
    private String entityCode;

    /**
     * Entity identifier.
     */
    private Long entityId;

    /**
     * Metric write list.
     */
    private List<MetricWriteDTO> metricList;

    /**
     * Snapshot timestamp in milliseconds.
     */
    private Long snapshotTs;
}

