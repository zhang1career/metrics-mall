package lab.zhang.data_science.metrics_mall.pojo.dao.y_group;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lab.zhang.data_science.metrics_mall.pojo.dao.BaseDAO;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * YGroup definition entity.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("y_group")
public class MetricDimensionGroupRelDAO extends BaseDAO {

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
}

