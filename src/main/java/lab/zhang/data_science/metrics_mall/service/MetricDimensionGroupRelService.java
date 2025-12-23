package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.common.OrderedList;
import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * YGroup service interface.
 *
 * @author Rongjin Zhang
 */
public interface MetricDimensionGroupRelService {

    /**
     * Get yGroup by id.
     *
     * @param id yGroup id
     * @return yGroup model
     */
    MetricDimensionGroupRel get(Long id);

    /**
     * List all yGroups.
     *
     * @return yGroup model list
     */
    List<MetricDimensionGroupRel> list();

    /**
     * List yGroups by metric id.
     *
     * @param metricId metric id
     * @return yGroup model list
     */
    List<MetricDimensionGroupRel> listByMetricId(Long metricId);

    /**
     * Map dimension ids by metric code batch.
     *
     * @param metricCodeColl metric code collection
     * @return map of metric code to ordered list of dimension ids
     */
    Map<String, Set<OrderedList<String>>> mapDimensionIdsByMetricCodeBatch(Collection<String> metricCodeColl);

    /**
     * Count all yGroups.
     *
     * @return yGroup count
     */
    long count();

    /**
     * Insert a new yGroup.
     *
     * @param dto yGroup data transfer object
     * @return true if success
     */
    boolean insert(MetricDimensionGroupRelDTO dto);

    /**
     * Update an existing yGroup.
     *
     * @param dto yGroup data transfer object
     * @return true if success
     */
    boolean update(MetricDimensionGroupRelDTO dto);

    /**
     * Delete a yGroup by id.
     *
     * @param id yGroup id
     * @return true if success
     */
    boolean delete(Long id);
}

