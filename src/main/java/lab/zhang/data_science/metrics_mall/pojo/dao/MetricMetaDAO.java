package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Metric meta information entity.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("metric_meta")
public class MetricMetaDAO extends BaseDAO {

    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Metric code (unique).
     */
    private String code;

    /**
     * Metric name in Chinese.
     */
    private String name;

    /**
     * Metric description.
     */
    private String description;

    /**
     * Metric type: 0=ATOMIC, 1=DERIVED, 2=COMPOSITE.
     */
    private Integer metricType;

    /**
     * Value type: 0=STR, 1=INT, 2=LONG, 3=FLOAT, 4=BOOL, 5=DATE, 6=OBJ.
     */
    private Integer valueType;

    /**
     * Decimal precision for FLOAT type metrics.
     */
    @TableField("`precision`")
    private Integer precision;

    /**
     * Aggregation type: 0=sum, 1=avg, 2=max, 3=min, 4=count.
     */
    @TableField("agg_type")
    private Integer aggregationType;

    /**
     * Unit of the metric.
     */
    private String unit;

    /**
     * Valid range of metric values.
     */
    private String validation;
}

