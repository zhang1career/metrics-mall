package lab.zhang.data_science.metrics_mall.model;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Metric model for service layer.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metric extends BaseModel {

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
     * Metric type.
     */
    private MetricTypeEnum metricType;

    /**
     * Metric value.
     */
    private TypedValue value;

    /**
     * Precision for decimal value.
     */
    private Integer precision;

    /**
     * History metric values list, each entry is a pair of metric value and timestamp.
     */
    private List<SampledValue> historyValueList;

    /**
     * Aggregation type
     */
    private AggregationTypeEnum aggregationType;

    /**
     * Unit of the metric.
     */
    private String unit;

    /**
     * Valid range of metric values.
     */
    private Map<String, TypedValue> validationMap;


    /**
     * Sampled value model of a metric.
     *
     * @author Rongjin Zhang
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SampledValue extends BaseModel {

        /**
         * Metric code.
         */
        private String code;

        /**
         * Metric value.
         */
        private TypedValue value;

        /**
         * Metric sample timestamp.
         */
        private LocalDateTime sampleTime;
    }
}

