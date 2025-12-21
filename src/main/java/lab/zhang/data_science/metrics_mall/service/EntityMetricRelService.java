package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetricRelDTO;

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
     * @param dto entity metric relation DTO
     * @return true if success
     */
    boolean create(EntityMetricRelDTO dto);

    /**
     * Update entity metric relation.
     *
     * @param dto entity metric relation DTO
     * @return true if success
     */
    boolean update(EntityMetricRelDTO dto);

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
     * Delete entity metric relation by entity meta id and metric meta id.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @return true if success
     */
    boolean delete(Long entityMetaId, Long metricMetaId);
}

