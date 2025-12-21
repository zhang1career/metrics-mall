package lab.zhang.data_science.metrics_mall.pojo.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
abstract public class BaseDAO {

    /**
     * Create time (UNIX timestamp in milliseconds)
     */
    protected Long ct;

    /**
     * Update time (UNIX timestamp in milliseconds)
     */
    protected Long ut;
}
