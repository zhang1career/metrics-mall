package lab.zhang.data_science.metrics_mall.pojo.dao.metric_version;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Metric and Dimension existence relation DAO.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExistenceMetricVersionDAO {

    /**
     * Metric code.
     */
    private String metricCode;

    /**
     * Version number.
     */
    private Integer version;

    /**
     * Whether this version is the main version: 0=no, 1=yes.
     */
    private Integer isMain;
}
