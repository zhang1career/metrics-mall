package lab.zhang.data_science.metrics_mall.pojo.dao.y_group;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * YGroup definition entity.
 *
 * @author Rongjin Zhang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("y_group")
public class MetricDimensionGroupRelDAO {

    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Metric meta id.
     */
    private Long mid;

    /**
     * Dimension ids, comma separated.
     */
    private String dids;

    /**
     * Create time (UNIX timestamp in milliseconds)
     */
    private Long ct;
}

