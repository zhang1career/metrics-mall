package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelResultDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetricRelDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * List entity metric relations by alias list.
     *
     * @param entityMetaId entity id
     * @param aliasColl alias list
     * @return entity metric relation querying result DAO map
     */
    Map<String, EntityMetricRelResultDAO> mapByAliasBatch(Long entityMetaId, Collection<String> aliasColl);

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

