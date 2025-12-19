package lab.zhang.data_science.metrics_mall.pojo.dto.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * The fundamental unit of metric data access object
 *
 * @author Rongjin Zhang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlphaMetricDTO {

    /**
     * Metric value.
     */
    private String value;
}
