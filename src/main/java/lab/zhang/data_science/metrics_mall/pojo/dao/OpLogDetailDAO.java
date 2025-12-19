package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Operation log entity.
 *
 * @author Rongjin Zhang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_log_detail")
public class OpLogDetailDAO {

    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Operation log id.
     */
    private Long opLogId;

    /**
     * Operation target table.
     */
    private Integer targetTable;

    /**
     * Operation target id.
     */
    private Long targetId;

    /**
     * Snapshot before operation, json format.
     */
    private String src;

    /**
     * Modification, key-value pattern, json format.
     */
    private String mod;

    /**
     * Create time (UNIX timestamp in milliseconds).
     */
    private Long ct;
}

