package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Time range query object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangeQO {

    /**
     * Start time, format: "yyyy-MM-dd HH:mm:ss".
     */
    @NotBlank(message = "Start time cannot be blank")
    private String start;

    /**
     * End time, format: "yyyy-MM-dd HH:mm:ss".
     */
    @NotBlank(message = "End time cannot be blank")
    private String end;
}
