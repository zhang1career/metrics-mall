package lab.zhang.data_science.metrics_mall.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * YGroup model.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDimensionGroupRel extends BaseModel {

    private Long id;

    private Long metricId;

    private List<String> dimensionIdList;
}

