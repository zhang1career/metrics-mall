package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;

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
     * Get metric version by metric id and version number.
     *
     * @param metricId metric id
     * @param version version number
     * @return metric version model
     */
    MetricVersion getByMetricIdAndVersion(Long metricId, Integer version);

    /**
     * List metric versions by query criteria.
     *
     * @param dto query criteria
     * @return list of metric version model
     */
    List<MetricVersion> list(MetricVersionDTO dto);

    /**
     * Insert a new metric version.
     *
     * @param dto metric version data transfer object
     * @return true if success
     */
    boolean insert(MetricVersionDTO dto);

    /**
     * Update an existing metric version.
     *
     * @param dto metric version data transfer object
     * @return true if success
     */
    boolean update(MetricVersionDTO dto);

    /**
     * Delete a metric version by id.
     *
     * @param id primary key id
     * @return true if success
     */
    boolean delete(Long id);
}

