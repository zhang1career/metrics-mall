package lab.zhang.data_science.metrics_mall.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event acceptance result.
 */
@Data
@AllArgsConstructor
public class EventAcceptanceResult {
    private final Integer accepted;
    private final Integer rejected;
}
