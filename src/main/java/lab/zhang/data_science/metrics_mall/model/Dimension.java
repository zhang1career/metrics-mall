package lab.zhang.data_science.metrics_mall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Dimension definition entity.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dim")
public class Dimension extends BaseModel {

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

    /**
     * Max value of cardinality.
     */
    private Integer cardLimit;
}

