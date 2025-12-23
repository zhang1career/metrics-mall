package lab.zhang.data_science.metrics_mall.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMeta {

    private Long id;

    private String code;

    private String name;

    private String description;
}
