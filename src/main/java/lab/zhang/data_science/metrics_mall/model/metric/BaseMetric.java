package lab.zhang.data_science.metrics_mall.model.metric;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMetric {

    /**
     * Metric code.
     */
    private String code;

    /**
     * Metric value.
     */
    private TypedValue value;

    /**
     * Precision for decimal value.
     */
    private Integer precision;

    /**
     * Unit of the metric.
     */
    private String unit;

    /**
     * Metric sample timestamp.
     */
    private LocalDateTime sampleTime;

    /**
     * Valid range of metric values.
     */
    private Map<String, TypedValue> validationMap;
}
