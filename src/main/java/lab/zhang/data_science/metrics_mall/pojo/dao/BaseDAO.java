package lab.zhang.data_science.metrics_mall.pojo.dao;

import lab.zhang.data_science.metrics_mall.util.TimeUtil;
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


    public void setTimeOnCreate() {
        // Set time fields (UNIX timestamp in seconds)
        long currentTime = TimeUtil.getCurrentTime();
        this.ct = currentTime;
        this.ut = currentTime;
    }

    public void setTimeOnUpdate() {
        // Set update time (UNIX timestamp in seconds)
        this.ut = TimeUtil.getCurrentTime();
    }
}
