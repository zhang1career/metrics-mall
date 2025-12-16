package lab.zhang.data_science.metrics_mall.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Metric DTO for controller layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricVO extends BaseVO {

    /**
     * Primary key id.
     */
    private Long id;

    /**
     * Metric code (unique).
     */
    private String code;

    /**
     * Metric name in Chinese.
     */
    private String name;

    /**
     * Metric description.
     */
    private String description;

    /**
     * Metric type: 0=ATOMIC, 1=DERIVED, 2=COMPOSITE.
     */
    private Integer metricType;

    /**
     * Value type: 0=STR, 1=INT, 2=LONG, 3=FLOAT, 4=BOOL, 5=DATE, 6=OBJ.
     */
    private Integer valueType;

    /**
     * Precision for decimal display.
     */
    private Integer precision;

    /**
     * Aggregation type: 0=sum, 1=avg, 2=max, 3=min, 4=count.
     */
    @JsonProperty("agg_type")
    private Integer aggregationType;

    /**
     * Unit of the metric.
     */
    private String unit;

    /**
     * Valid range of metric values.
     */
    private Map<String, TypedValue> validation;


    /**
     * Metric brief view object for controller layer.
     *
     * @author Rongjin Zhang
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricPrettyBriefVO {

        /**
         * Metric name in Chinese.
         */
        private String name;

        /**
         * Metric description.
         */
        private String description;

        /**
         * Unit of the metric.
         */
        private String unit;

        /**
         * Precision for decimal display.
         */
        private Integer precision;

        /**
         * Aggregation type: sum, avg, max, min, count.
         */
        @JsonProperty("agg_type_str")
        private String aggregationTypeStr;
    }
}

