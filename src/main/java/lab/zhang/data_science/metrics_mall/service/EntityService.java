package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.Entity.EntityMeta;

public interface EntityService {

    /**
     * Get entity meta by entity code.
     * @param entityCode entity code
     * @return entity meta
     */
    EntityMeta getEntityMetaByCode(String entityCode);


    /**
     * Get entity by code and id.
     *
     * @param entityCode entity code
     * @param entityId   entity id
     * @return entity model
     */
    Entity getEntityByCode(String entityCode, Long entityId);
}
