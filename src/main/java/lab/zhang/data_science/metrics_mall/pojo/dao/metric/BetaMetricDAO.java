package lab.zhang.data_science.metrics_mall.pojo.dao.metric;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * Beta metric data access object
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class BetaMetricDAO extends AlphaMetricDAO {
}
