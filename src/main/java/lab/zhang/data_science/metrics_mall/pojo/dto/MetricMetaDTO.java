package lab.zhang.data_science.metrics_mall.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Metric data transfer object.
 *
 * @author Rongjin Zhang
 * @date 2025-12-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MetricMetaDTO extends BaseDTO {

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

    private Map<String, TypedValue> validation;

    private Integer cardMax;
}

