package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event query object for controller layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBatchQO {
    
    /**
     * Event list.
     */
    @NotNull(message = "Events cannot be null")
    @Valid
    private List<EventQO> events;

}

