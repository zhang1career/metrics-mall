package lab.zhang.data_science.metrics_mall.model;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.*;

import java.time.LocalDateTime;

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
public class SampledValue extends BaseModel {

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
