package lab.zhang.data_science.metrics_mall.pojo.dto;

import lab.zhang.data_science.metrics_mall.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
abstract public class BaseDTO {

    /**
     * Creation time.
     */
    Date createTime;

    /**
     * Update time.
     */
    Date updateTime;


    public void setTimeOnCreate() {
        // Set time fields (UNIX timestamp in seconds)
        long currentTime = TimeUtil.getCurrentTime();
        this.createTime = new Date(currentTime);
        this.updateTime = new Date(currentTime);
    }

    public void setTimeOnUpdate() {
        // Set update time (UNIX timestamp in seconds)
        long currentTime = TimeUtil.getCurrentTime();
        this.updateTime = new Date(currentTime);
    }
}
