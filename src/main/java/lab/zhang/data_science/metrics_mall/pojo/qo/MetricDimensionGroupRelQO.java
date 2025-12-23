package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * YGroup query object.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionGroupRelQO {

    private Long metricId;

    @NotNull(message = "MetricCode cannot be null", groups = {Create.class, Update.class, Query.class})
    private String metricCode;

    private String dimensionIds;

    @NotNull(message = "DimensionCodes cannot be null", groups = {Create.class, Update.class})
    private String dimensionCodes;


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

