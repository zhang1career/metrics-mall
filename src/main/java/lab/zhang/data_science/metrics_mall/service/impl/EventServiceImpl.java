package lab.zhang.data_science.metrics_mall.service.impl;

import lab.zhang.data_science.metrics_mall.model.EventAcceptanceResult;
import lab.zhang.data_science.metrics_mall.pojo.dto.EventDTO;
import lab.zhang.data_science.metrics_mall.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Event service implementation.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    @Override
    public EventAcceptanceResult processEventBatch(List<EventDTO> eventDTOList) {
        if (eventDTOList == null || eventDTOList.isEmpty()) {
            return new EventAcceptanceResult(0, 0);
        }

        int accepted = 0;
        int rejected = 0;

        for (EventDTO eventDTO : eventDTOList) {
            if (validateEvent(eventDTO)) {
                accepted++;
                // TODO: Send event to Kafka for processing
                // In MVP stage, we just validate and accept events
            } else {
                rejected++;
                log.warn("Event rejected: metricName={}, timestamp={}", 
                        eventDTO.getMetricName(), eventDTO.getTimestamp());
            }
        }

        return new EventAcceptanceResult(accepted, rejected);
    }

    /**
     * Validate event data.
     *
     * @param eventDTO event DTO
     * @return true if valid, false otherwise
     */
    private boolean validateEvent(EventDTO eventDTO) {
        if (eventDTO == null) {
            return false;
        }
        if (eventDTO.getMetricName() == null || eventDTO.getMetricName().trim().isEmpty()) {
            return false;
        }
        if (eventDTO.getTimestamp() == null || eventDTO.getTimestamp() <= 0) {
            return false;
        }
        if (eventDTO.getValue() == null) {
            return false;
        }
        return true;
    }
}

