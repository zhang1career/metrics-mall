package lab.zhang.data_science.metrics_mall.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric brief view object for controller layer.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BriefMetricVO {

    private String code;

    private String unit;

    private Integer precision;


    /**
     * Metric pretty brief information.
     *
     * @author Rongjin Zhang
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrettyBriefMetricVO {

        /**
         * Metric name in Chinese.
         */
        private String name;

        /**
         * Metric description.
         */
        private String description;

        /**
         * Unit of the metric.
         */
        private String unit;

        /**
         * Precision for decimal display.
         */
        private Integer precision;

        /**
         * Aggregation type: sum, avg, max, min, count.
         */
        @JsonProperty("agg_type_str")
        private String aggregationTypeStr;
    }
}
