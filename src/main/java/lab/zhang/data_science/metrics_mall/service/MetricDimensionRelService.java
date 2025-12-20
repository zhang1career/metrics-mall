package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO;

import java.util.List;

/**
 * Metric Dimension Relation service interface.
 *
 * @author Rongjin Zhang
 */
public interface MetricDimensionRelService {

    /**
     * Create metric dimension relation.
     *
     * @param metricMetaId metric meta id
     * @param dimensionId dimension id
     * @param isHot is hot dimension
     * @param validation validation rule
     * @return true if success
     */
    boolean create(Long metricMetaId, Long dimensionId, Integer isHot, String validation);

    /**
     * Get metric dimension relation by metric meta id and dimension id.
     *
     * @param metricMetaId metric meta id
     * @param dimensionId dimension id
     * @return metric dimension relation DAO, null if not found
     */
    MetricDimensionRelDAO get(Long metricMetaId, Long dimensionId);

    /**
     * List metric dimension relations by metric meta id.
     *
     * @param metricMetaId metric meta id
     * @return metric dimension relation DAO list
     */
    List<MetricDimensionRelDAO> listByMetricMetaId(Long metricMetaId);

    /**
     * List metric dimension relations by dimension id.
     *
     * @param dimensionId dimension id
     * @return metric dimension relation DAO list
     */
    List<MetricDimensionRelDAO> listByDimensionId(Long dimensionId);

    /**
     * List all metric dimension relations.
     *
     * @return metric dimension relation DAO list
     */
    List<MetricDimensionRelDAO> list();

    /**
     * Update metric dimension relation.
     *
     * @param metricMetaId metric meta id
     * @param dimensionId dimension id
     * @param isHot is hot dimension
     * @param validation validation rule
     * @return true if success
     */
    boolean update(Long metricMetaId, Long dimensionId, Integer isHot, String validation);

    /**
     * Delete metric dimension relation by metric meta id and dimension id.
     *
     * @param metricMetaId metric meta id
     * @param dimensionId dimension id
     * @return true if success
     */
    boolean delete(Long metricMetaId, Long dimensionId);
}

