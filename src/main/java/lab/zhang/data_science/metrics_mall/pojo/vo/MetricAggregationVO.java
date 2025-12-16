package lab.zhang.data_science.metrics_mall.pojo.vo;

import lab.zhang.data_science.metrics_mall.pojo.vo.BriefMetricVO.PrettyBriefMetricVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Metric aggregation VO for controller layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricAggregationVO {

    /**
     * Metric metadata map, key is metric code, value is metric pretty brief information.
     */
    private Map<String, PrettyBriefMetricVO> meta;

    /**
     * Aggregation result rows.
     */
    private List<AggregationVO> rows;

}
