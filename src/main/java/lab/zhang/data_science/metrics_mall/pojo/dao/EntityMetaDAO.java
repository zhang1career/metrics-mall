package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * entity meta entity.
 *
 * @author Rongjin Zhang
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("entity_meta")
public class EntityMetaDAO extends BaseDAO {
    
    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * Entity code (unique), e.g., user, device, trans.
     */
    private String code;
    
    /**
     * Entity name in Chinese, e.g., 用户, 设备, 交易单.
     */
    private String name;

    /**
     * Entity description.
     */
    private String description;
}

