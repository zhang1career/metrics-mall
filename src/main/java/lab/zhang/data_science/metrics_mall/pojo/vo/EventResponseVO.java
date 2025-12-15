package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event response DTO for controller layer.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseVO {
    
    /**
     * Number of accepted events.
     */
    private Integer accepted;
    
    /**
     * Number of rejected events.
     */
    private Integer rejected;
}

