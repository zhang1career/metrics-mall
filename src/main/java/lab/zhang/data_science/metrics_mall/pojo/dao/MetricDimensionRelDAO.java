package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Metric_meta - Dimension Relation DAO for database
 *
 * @author Rongjin Zhang
 */
@Data
@TableName("y")
public class MetricDimensionRelDAO {
    /**
     * Dimension ID (part of composite primary key, unsigned integer)
     */
    @TableId("mid")
    private Long metricMetaId;

    /**
     * Dimension ID (part of composite primary key, unsigned integer)
     */
    @TableField("did")
    private Long dimensionId;

    /**
     * Is hot dimension
     */
    private Integer isHot;

    /**
     * Valid range of dimension values, JSON format.
     */
    private String validation;
}

