package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.Entity;

public interface EntityService {
    /**
     * Get entity by code and id.
     *
     * @param entityCode entity code
     * @param entityId   entity id
     * @return entity model
     */
    Entity getEntity(String entityCode, Long entityId);
}
