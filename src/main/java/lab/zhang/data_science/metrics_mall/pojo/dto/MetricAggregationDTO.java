package lab.zhang.data_science.metrics_mall.pojo.dto;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    private Set<String> metricCodeSet;

    private LocalDateTime startTime;

    private LocalDateTime stopTime;

    private Long intervalInSeconds;

    private List<String> groupByList;
    
    private List<FieldConditionDTO> filterList;
    
    private List<OrderByDTO> orderByList;
    
    private Integer limit;


    /**
     * Filter model.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldConditionDTO {

        /**
         * Field name.
         */
        private String field;

        /**
         * Operator: =, !=, >, <, >=, <=, in, not_in.
         */
        private String op;

        /**
         * Filter value.
         */
        private TypedValue value;
    }


    /**
     * Order by model.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderByDTO {

        /**
         * Field name.
         */
        private String field;

        /**
         * Sort direction: asc, desc.
         */
        private String sort;
    }
}
