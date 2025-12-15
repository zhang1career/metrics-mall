package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Execution event relation entity for database (table x)
 * Represents the relationship between events, rules and rule groups
 *
 * @author Rongjin Zhang
 */
@Data
@TableName("x")
public class EntityMetaDimensionRelationDAO {

    /**
     * Entity ID (part of composite primary key, unsigned integer)
     */
    @TableId
    @TableField("eid")
    private Long entityId;

    /**
     * Dimension ID (part of composite primary key)
     */
    @TableId
    @TableField("did")
    private Long dimensionId;

    /**
     * Dimension alias name
     */
    private String alias;

    /**
     * Data URI
     */
    private String dataUri;

    /**
     * Is hot dimension
     */
    private Integer isHot;
}

