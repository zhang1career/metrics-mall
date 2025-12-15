package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Metric query item query object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionedMetricDimensionQO {

    /**
     * Metric code.
     */
    @NotBlank(message = "Metric code cannot be blank")
    private String code;

    /**
     * Metric version.
     */
    private Integer v;

    /**
     * Dimension conditions map.
     */
    private Map<String, Object> dims;
}
