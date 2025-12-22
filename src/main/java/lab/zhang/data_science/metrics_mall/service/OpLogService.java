package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.OpLog;
import lab.zhang.data_science.metrics_mall.pojo.dto.OpLogDTO;

import java.util.List;

/**
 * Operation log service interface.
 *
 * @author Rongjin Zhang
 */
public interface OpLogService {

    /**
     * Get operation log by id.
     *
     * @param id log id
     * @return operation log model
     */
    OpLog get(Long id);

    /**
     * List operation logs.
     *
     * @return list of operation log models
     */
    List<OpLog> list();

    /**
     * Count total operation logs.
     *
     * @return count of operation logs
     */
    Long count();

    /**
     * Insert a new operation log.
     *
     * @param dto operation log data transfer object
     * @return inserted operation log model with id and create time
     */
    OpLog insert(OpLogDTO dto);

    /**
     * Update an existing operation log.
     *
     * @param dto operation log data transfer object
     * @return true if update success
     */
    boolean update(OpLogDTO dto);

    /**
     * Delete an operation log by id.
     *
     * @param id log id
     * @return true if delete success
     */
    boolean delete(Long id);
}

