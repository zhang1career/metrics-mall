package lab.zhang.data_science.metrics_mall.pojo.qo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dimension query object.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionQO {

    private Long id;

    @NotBlank(message = "Dimension code cannot be blank")
    private String code;

    @NotBlank(message = "Dimension name cannot be blank")
    private String name;

    private String validation;
}

