package lab.zhang.data_science.metrics_mall.model.metric;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

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
public class EchoMetric extends BetaMetric {
    /**
     * History metric values list
     */
    private List<AlphaMetric> historyList;

    /**
     * Valid range of metric values.
     */
    private Map<String, TypedValue> validationMap;


    /**
     * Get the latest sampled value before or at the given snapshot time.
     *
     * @param snapshotTs the snapshot time
     * @return the latest sampled value before or at the snapshot time, or null if none found
     */
    public AlphaMetric getBackNearest(Long snapshotTs) {
        List<AlphaMetric> tempList = new ArrayList<>(List.of(new AlphaMetric(
                this.getCode(), this.getValue(), this.getSnapshotTs(), this.getSourceType())));
        if (this.historyList != null) {
            tempList.addAll(this.historyList);
        }
        return tempList.stream()
                .filter(sv -> sv.getSnapshotTs() <= snapshotTs)
                .max(Comparator.comparing(AlphaMetric::getSnapshotTs))
                .orElse(null);
    }
}
