package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Metric query object for CRUD operations.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricMetaQO {

    private Long id;

    @NotBlank(message = "Metric code cannot be blank")
    private String code;

    @NotBlank(message = "Metric name cannot be blank")
    private String name;

    private String description;

    private Integer metricType;

    private Integer valueType;

    private Integer precision;

    private Integer aggregationType;

    private String unit;

    private Map<String, Object> validation;

    private Integer cardMax;
}

