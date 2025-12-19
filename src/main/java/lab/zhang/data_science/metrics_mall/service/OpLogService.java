package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.OpLog;

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
     * @param model operation log model
     * @return inserted operation log model with id and create time
     */
    OpLog insert(OpLog model);

    /**
     * Update an existing operation log.
     *
     * @param model operation log model
     * @return true if update success
     */
    boolean update(OpLog model);

    /**
     * Delete an operation log by id.
     *
     * @param id log id
     * @return true if delete success
     */
    boolean delete(Long id);
}

