package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity meta data transfer object.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetaDTO extends BaseDTO {

    private Integer id;

    private String code;

    private String name;

    private String description;
}

