package lab.zhang.data_science.metrics_mall.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity meta view object.
 *
 * @author Rongjin Zhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetaVO {

    private Integer id;

    private String code;

    private String name;

    private String description;

    private Long ct;

    private Long ut;
}

