package lab.zhang.data_science.metrics_mall.pojo.dao.y_group;

import lab.zhang.data_science.metrics_mall.pojo.dao.BaseDAO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
public class MetricDimensionGroupRelResultDAO extends BaseDAO {

    private String metricCode;

    private String dimensionIds;
}

