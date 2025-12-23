package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * YGroup data transfer object.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionGroupRelDTO extends BaseDTO {

    private Long id;

    private Long metricId;

    private List<String> dimensionIdList;
}

