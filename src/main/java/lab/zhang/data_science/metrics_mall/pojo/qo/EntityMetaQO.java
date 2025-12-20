package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity meta query object.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetaQO {

    private Integer id;

    @NotBlank(message = "Entity code cannot be blank")
    private String code;

    @NotBlank(message = "Entity name cannot be blank")
    private String name;

    private String description;
}

