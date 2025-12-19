package lab.zhang.data_science.metrics_mall.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Dimension model.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Dimension extends BaseModel {

    private Long id;

    private String code;

    private String name;

    private String validation;
}

