package lab.zhang.data_science.metrics_mall.pojo.dao.metric;

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
public class AlphaMetricDAO {

    /**
     * Metric value.
     */
    private String a;

    /**
     * Metric snapshot timestamp, in milliseconds.
     */
    private Long ts;

    /**
     * Source type of metric value
     */
    private Integer s;
}
