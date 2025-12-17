package lab.zhang.data_science.metrics_mall.pojo.dao.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * echo metric data access object
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EchoMetricDAO extends BetaMetricDAO {
    /**
     * History metric values list
     */
    private List<AlphaMetricDAO> h;

}
