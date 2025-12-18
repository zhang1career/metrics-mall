package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EchoMetricQO {

    /**
     * Metric code, the identifier of the metric.
     */
    @NotBlank(message = "Metric code cannot be blank", groups = {Create.class, Update.class})
    private String code;

    /**
     * Metric alias, a user-friendly name for the metric.
     */
    @NotBlank(message = "Metric alias cannot be blank")
    private String alias;

    /**
     * Metric version.
     * Mull or 0 means latest version.
     */
    private Integer v;

    /**
     * Dimension conditions map.
     */
    private Map<String, Object> dims;

    /**
     * Metric value.
     */
    @NotNull(message = "Metric value cannot be null", groups = {Create.class, Update.class})
    private Object value;


    /**
     * Validation group for create operation
     */
    public interface Create {
    }

    /**
     * Validation group for update operation
     */
    public interface Update {
    }

    /**
     * Validation group for query operation
     */
    public interface Query {
    }
}
