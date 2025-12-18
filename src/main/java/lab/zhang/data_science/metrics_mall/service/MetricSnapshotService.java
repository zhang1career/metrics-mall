package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO;

/**
 * Metric service interface.
 *
 * @author Rongjin Zhang
 */
public interface MetricSnapshotService {
    /**
     * Query metric snapshot values.
     *
     * @param dto metric snapshot query model
     * @return metric snapshot result containing values and timestamps
     */
    MetricSnapshot querySnapshot(MetricSnapshotDTO dto);

    /**
     * Write metric snapshot values.
     *
     * @param dto metric snapshot write model
     * @return op_log id
     */
    Long writeSnapshot(MetricSnapshotWriteDTO dto);
}

