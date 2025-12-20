package lab.zhang.data_science.metrics_mall.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity meta model.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMeta extends BaseModel {

    private Integer id;

    private String code;

    private String name;

    private String description;
}

