package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.EntityMeta;

import java.util.List;

/**
 * Entity meta service interface.
 *
 * @author Rongjin Zhang
 */
public interface EntityMetaService {

    /**
     * Get entity meta by id.
     *
     * @param id entity meta id
     * @return entity meta model
     */
    EntityMeta get(Integer id);

    /**
     * Get entity meta by code.
     *
     * @param code entity meta code
     * @return entity meta model
     */
    EntityMeta getByCode(String code);

    /**
     * List all entity metas.
     *
     * @return entity meta model list
     */
    List<EntityMeta> list();

    /**
     * Count all entity metas.
     *
     * @return entity meta count
     */
    long count();

    /**
     * Insert a new entity meta.
     *
     * @param entityMeta entity meta model
     * @return true if success
     */
    boolean insert(EntityMeta entityMeta);

    /**
     * Update an existing entity meta.
     *
     * @param entityMeta entity meta model
     * @return true if success
     */
    boolean update(EntityMeta entityMeta);

    /**
     * Delete an entity meta by id.
     *
     * @param id entity meta id
     * @return true if success
     */
    boolean delete(Integer id);
}

