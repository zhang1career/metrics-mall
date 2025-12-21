package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Dimension query object.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDTO extends BaseDTO {

    private Long id;

    private String code;

    private String name;

    private String validation;
}

