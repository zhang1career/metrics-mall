package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event item query object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventQO {

    /**
     * Metric name.
     */
    @NotBlank(message = "Metric name cannot be blank")
    private String metricName;

    /**
     * Event timestamp in milliseconds.
     */
    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;

    /**
     * Dimension map.
     */
    private Map<String, String> dim;

    /**
     * Metric value.
     */
    @NotNull(message = "Value cannot be null")
    private Double value;
}
