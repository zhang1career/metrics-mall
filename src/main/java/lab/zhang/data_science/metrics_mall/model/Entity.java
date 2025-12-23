package lab.zhang.data_science.metrics_mall.model;

import lombok.*;
import lombok.experimental.SuperBuilder;


/**
 * Entity is an abstraction of a business object in the metrics mall system.
 * It can represent various real-world entities such as users, products, or transactions.
 * The Entity class contains metadata about the entity and a unique identifier.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Entity extends BaseModel {

    private EntityMeta meta;

    private Long id;
}
