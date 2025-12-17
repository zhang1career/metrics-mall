package lab.zhang.data_science.metrics_mall.model.metric;

import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * Primary metric model, defines the core attributes of a metric.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrimeMetric extends BetaMetric {

    /**
     * Primary key id.
     */
    private Long id;

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
     * Aggregation type
     */
    private AggregationTypeEnum aggregationType;

    /**
     * Creation time.
     */
    Date createTime;

    /**
     * Update time.
     */
    Date updateTime;
}

