package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event model for service layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    
    /**
     * Metric name.
     */
    private String metricName;
    
    /**
     * Event timestamp in milliseconds.
     */
    private Long timestamp;
    
    /**
     * Dimension map.
     */
    private Map<String, String> dimensionMap;
    
    /**
     * Metric value.
     */
    private Double value;
}

