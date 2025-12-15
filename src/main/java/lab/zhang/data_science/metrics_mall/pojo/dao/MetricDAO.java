package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.*;

import java.util.Map;

/**
 * Metric definition entity.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("metric")
public class MetricDAO extends BaseDAO {

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
    private Map<String, TypedValue> validation;
}

