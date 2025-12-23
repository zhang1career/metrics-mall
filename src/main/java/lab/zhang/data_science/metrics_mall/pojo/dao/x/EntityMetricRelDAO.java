package lab.zhang.data_science.metrics_mall.pojo.dao.x;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Entity_meta - Metric_meta Relation DAO for database
 *
 * @author Rongjin Zhang
 */
@Data
@TableName("x")
public class EntityMetricRelDAO {

    /**
     * Entity ID (part of composite primary key, unsigned integer)
     */
    @TableId("eid")
    private Long entityMetaId;

    /**
     * Dimension ID (part of composite primary key, unsigned integer)
     */
    @TableField("mid")
    private Long metricMetaId;

    /**
     * Dimension alias name
     */
    private String alias;

    /**
     * Data URI
     */
    private String dataUri;
}

