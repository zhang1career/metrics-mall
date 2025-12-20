package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Metric Relation query object for create operation.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetricRelQO {

    @NotBlank(message = "Entity code cannot be blank")
    private String entityCode;

    @NotBlank(message = "Metric code cannot be blank")
    private String metricCode;

    @NotBlank(message = "Alias cannot be blank")
    private String alias;

    @NotBlank(message = "Data URI cannot be blank")
    private String dataUri;
}
