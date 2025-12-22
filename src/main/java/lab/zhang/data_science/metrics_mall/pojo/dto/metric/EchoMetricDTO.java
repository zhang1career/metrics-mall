package lab.zhang.data_science.metrics_mall.pojo.dto.metric;

import cn.hutool.core.collection.ListUtil;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Multifaceted Metric query item model for service layer.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EchoMetricDTO extends BetaMetricDTO {

    /**
     * Metric code.
     */
    private String code;

    /**
     * Metric version.
     */
    private Integer version;

    /**
     * Dimension conditions map.
     */
    private Map<String, TypedValue> dimensionMap;


    /**
     * Get dimension code list.
     * @return dimension code list
     */
    public List<String> getDimensionCodeList() {
        if (dimensionMap == null) {
            return ListUtil.empty();
        }
        return dimensionMap.keySet().stream().toList();
    }
}
