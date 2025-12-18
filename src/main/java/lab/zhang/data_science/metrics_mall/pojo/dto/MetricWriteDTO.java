package lab.zhang.data_science.metrics_mall.pojo.dto;

import cn.hutool.core.collection.ListUtil;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Metric write model for service layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricWriteDTO {

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
     * Metric value.
     */
    private String value;


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

