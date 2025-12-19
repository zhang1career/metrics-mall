package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;

/**
 * Operation log entity.
 *
 * @author Rongjin Zhang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_log")
public class OpLogDAO {

    /**
     * Primary key id.
     */
    @TableId(type = IdType.INPUT)
    private BigInteger id;

    /**
     * Event type.
     */
    private Integer event;

    /**
     * Operator id.
     */
    @TableField("uid")
    private Long operatorId;

    /**
     * Operating time (UNIX timestamp in milliseconds).
     */
    @TableField("ot")
    private Long operateTs;

    /**
     * Create time (UNIX timestamp in milliseconds).
     */
    private Long ct;
}

