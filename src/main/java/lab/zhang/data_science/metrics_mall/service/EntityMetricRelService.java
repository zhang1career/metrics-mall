package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;

import java.util.List;

/**
 * Entity Metric Relation service interface.
 *
 * @author Rongjin Zhang
 */
public interface EntityMetricRelService {

    /**
     * Create entity metric relation.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @param alias alias name
     * @param dataUri data URI
     * @return true if success
     */
    boolean create(Long entityMetaId, Long metricMetaId, String alias, String dataUri);

    /**
     * Get entity metric relation by entity meta id and metric meta id.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @return entity metric relation DAO, null if not found
     */
    EntityMetricRelDAO get(Long entityMetaId, Long metricMetaId);

    /**
     * List entity metric relations by entity meta id.
     *
     * @param entityMetaId entity meta id
     * @return entity metric relation DAO list
     */
    List<EntityMetricRelDAO> listByEntityMetaId(Long entityMetaId);

    /**
     * List entity metric relations by metric meta id.
     *
     * @param metricMetaId metric meta id
     * @return entity metric relation DAO list
     */
    List<EntityMetricRelDAO> listByMetricMetaId(Long metricMetaId);

    /**
     * List all entity metric relations.
     *
     * @return entity metric relation DAO list
     */
    List<EntityMetricRelDAO> list();

    /**
     * Update entity metric relation.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @param alias alias name
     * @param dataUri data URI
     * @return true if success
     */
    boolean update(Long entityMetaId, Long metricMetaId, String alias, String dataUri);

    /**
     * Delete entity metric relation by entity meta id and metric meta id.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @return true if success
     */
    boolean delete(Long entityMetaId, Long metricMetaId);
}

