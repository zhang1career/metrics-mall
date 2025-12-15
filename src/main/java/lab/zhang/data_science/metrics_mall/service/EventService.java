package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.pojo.dto.EventDTO;
import lab.zhang.data_science.metrics_mall.model.EventAcceptanceResult;

import java.util.List;

/**
 * Event service interface.
 *
 * @author Rongjin Zhang
 * 
 */
public interface EventService {
    
    /**
     * Process events and return acceptance result.
     *
     * @param eventDTOList event model list
     * @return accepted count and rejected count
     */
    EventAcceptanceResult processEventBatch(List<EventDTO> eventDTOList);
}

