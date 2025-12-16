package lab.zhang.data_science.metrics_mall.model.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Historic snapshot, multi-versioned, and multi-dimensioned metric model for a multifaceted data view.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EchoMetric extends BaseMetric {
    /**
     * History metric values list, each entry is a pair of metric value and timestamp.
     */
    private List<BaseMetric> historyMetricList;
}
