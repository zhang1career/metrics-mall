package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Metric version entity.
 *
 * @author Rongjin Zhang
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("metric_version")
public class MetricVersionDAO extends BaseDAO {
    
    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * Metric id (refer to metric).
     */
    @TableField("metric_id")
    private Long metricId;
    
    /**
     * Version number.
     */
    private Integer version;
    
    /**
     * Metric status: 0=OFFLINE, 1=DEV, 2=TEST, 3=GRAY, 4=ONLINE, 5=DEPRECATED.
     */
    @TableField("metric_status")
    private Integer metricStatus;
    
    /**
     * Timestamp when metric went online, UNIX timestamp in milliseconds.
     */
    @TableField("begin_status_t")
    private Long beginStatusTime;
    
    /**
     * Calculation logic.
     */
    private String calcLogic;
    
    /**
     * Whether this metric supports explainability: 0=no, 1=yes.
     */
    private Integer isExplainable;
    
    /**
     * Explainability type: 0=formula, 1=rule, 2=feature_importance, 3=lineage.
     */
    private Integer explainType;
    
    /**
     * Explainability configuration, JSON format.
     */
    private String explainConfig;
    
    /**
     * Change log.
     */
    private String changeLog;
}

