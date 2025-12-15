package lab.zhang.data_science.metrics_mall.model;

import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMeta extends BaseModel {

    private Integer id;

    private String code;

    private String name;

    private String description;
}
