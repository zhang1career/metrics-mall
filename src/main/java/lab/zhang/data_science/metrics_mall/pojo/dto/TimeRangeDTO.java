package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Time range model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangeDTO {

    /**
     * Start time, format: "yyyy-MM-dd HH:mm:ss".
     */
    private String start;

    /**
     * End time, format: "yyyy-MM-dd HH:mm:ss".
     */
    private String end;
}
