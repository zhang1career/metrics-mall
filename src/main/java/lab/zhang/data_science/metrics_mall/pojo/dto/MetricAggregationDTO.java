package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric aggregation query model for service layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricAggregationDTO {
    
    /**
     * Metric codes list.
     */
    private List<String> metricCodeList;
    
    /**
     * Time range.
     */
    private TimeRangeDTO timeRange;
    
    /**
     * Time interval, e.g., "1h", "1d".
     */
    private String interval;
    
    /**
     * Group by dimensions.
     */
    private List<String> groupBy;
    
    /**
     * Filter conditions.
     */
    private List<FieldConditionDTO> filters;
    
    /**
     * Order by configuration.
     */
    private OrderByDTO orderBy;
    
    /**
     * Result limit.
     */
    private Integer limit;

}

