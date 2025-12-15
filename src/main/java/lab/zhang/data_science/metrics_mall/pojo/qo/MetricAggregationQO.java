package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Metric aggregation query object for controller layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricAggregationQO {
    
    /**
     * Metric codes list.
     */
    @NotNull(message = "Metric codes cannot be null")
    private List<String> metricCodes;
    
    /**
     * Time range.
     */
    @NotNull(message = "Time range cannot be null")
    @Valid
    private TimeRangeQO timeRange;
    
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
    @Valid
    private List<FieldConditionQO> filters;
    
    /**
     * Order by configuration.
     */
    @Valid
    private OrderByQO orderBy;
    
    /**
     * Result limit.
     */
    private Integer limit;

}

