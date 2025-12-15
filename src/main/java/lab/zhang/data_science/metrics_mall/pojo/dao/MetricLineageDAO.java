package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Metric lineage entity.
 *
 * @author Rongjin Zhang
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("metric_lineage")
public class MetricLineageDAO extends BaseDAO {
    
    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * Source metric id (refer to metric.id).
     */
    private Long srcId;
    
    /**
     * Destination metric id (refer to metric.id).
     */
    private Long destId;
    
    /**
     * Depend type: 0=derived, 1=aggregated, 2=joined.
     */
    private Integer dependType;
    
    /**
     * SQL or Flink code to derive target metric from source metric(s).
     */
    private String transLogic;
}

