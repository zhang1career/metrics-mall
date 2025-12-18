package lab.zhang.data_science.metrics_mall.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Dimension definition entity.
 *
 * @author Rongjin Zhang
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dim")
public class DimensionDAO extends BaseDAO {
    
    /**
     * Primary key id.
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * Dimension code (unique).
     */
    private String code;
    
    /**
     * Dimension name in Chinese.
     */
    private String name;
    
    /**
     * Valid range of dimension values, JSON format.
     */
    private String validation;
}

