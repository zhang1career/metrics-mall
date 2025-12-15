package lab.zhang.data_science.metrics_mall.model;

import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entity extends BaseModel {

    private EntityMeta meta;

    private Integer id;
}
