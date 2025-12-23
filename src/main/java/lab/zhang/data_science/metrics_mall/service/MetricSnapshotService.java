package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;

import java.math.BigInteger;

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
     * Write metric snapshot values from external.
     *
     * @param dto metric snapshot write model
     * @return trace id of the write operation
     */
    BigInteger writeSnapshotExternal(MetricSnapshotDTO dto);

    /**
     * Write metric snapshot values from internal.
     *
     * @param dto metric snapshot write model
     * @return trace id of the write operation
     */
    BigInteger writeSnapshotInternal(MetricSnapshotDTO dto);

}

