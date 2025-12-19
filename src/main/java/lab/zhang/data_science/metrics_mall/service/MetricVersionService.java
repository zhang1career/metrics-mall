package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.MetricVersion;

import java.util.List;

/**
 * Metric version service interface.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
public interface MetricVersionService {

    /**
     * Get metric version by id.
     *
     * @param id primary key id
     * @return metric version model
     */
    MetricVersion get(Long id);

    /**
     * List metric versions by query criteria.
     *
     * @param queryModel query criteria
     * @return list of metric version model
     */
    List<MetricVersion> list(MetricVersion queryModel);

    /**
     * Insert a new metric version.
     *
     * @param model metric version model
     * @return true if success
     */
    boolean insert(MetricVersion model);

    /**
     * Update an existing metric version.
     *
     * @param model metric version model
     * @return true if success
     */
    boolean update(MetricVersion model);

    /**
     * Delete a metric version by id.
     *
     * @param id primary key id
     * @return true if success
     */
    boolean delete(Long id);
}

