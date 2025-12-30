package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric version query object.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricVersionQO {

    /**
     * Metric code.
     */
    private String metricCode;

    /**
     * Version number.
     */
    @NotNull(message = "Metric version cannot be null")
    @PositiveOrZero(message = "Metric version must be zero or positive")
    private Integer version;

    /**
     * Whether this version is the main version: 0=no, 1=yes.
     */
    private Integer isMain;

    /**
     * Life status: 0=OFFLINE, 1=DEV, 2=TEST, 3=GRAY, 4=ONLINE, 5=DEPRECATED.
     */
    private Integer lifeStatus;

    /**
     * Calculation logic.
     */
    private String calcLogic;
}

