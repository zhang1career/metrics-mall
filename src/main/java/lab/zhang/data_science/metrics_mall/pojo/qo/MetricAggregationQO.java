package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    private List<OrderByQO> orderBys;
    
    /**
     * Result limit.
     */
    private Integer limit;


    /**
     * Time range query object.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRangeQO {

        /**
         * Start time, format: "yyyy-MM-dd HH:mm:ss".
         */
        @NotBlank(message = "Start time cannot be blank")
        private String start;

        /**
         * Stop time, format: "yyyy-MM-dd HH:mm:ss".
         */
        @NotBlank(message = "Stop time cannot be blank")
        private String stop;
    }


    /**
     * Filter query object.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldConditionQO {

        /**
         * Field name.
         */
        @NotBlank(message = "Field cannot be blank")
        private String field;

        /**
         * Operator: =, !=, >, <, >=, <=, in, not_in.
         */
        @NotBlank(message = "Operator cannot be blank")
        private String op;

        /**
         * Filter value.
         */
        @NotNull(message = "Value cannot be null")
        private Object value;
    }


    /**
     * Order by query object.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderByQO {

        /**
         * Field name.
         */
        @NotBlank(message = "Field cannot be blank")
        private String field;

        /**
         * Sort direction: asc, desc.
         */
        @NotBlank(message = "Sort cannot be blank")
        private String sort;
    }
}
