package lab.zhang.data_science.metrics_mall.model.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BetaMetric extends AlphaMetric {

    /**
     * Precision for decimal value.
     */
    private Integer precision;

    /**
     * Unit of the metric.
     */
    private String unit;
}
