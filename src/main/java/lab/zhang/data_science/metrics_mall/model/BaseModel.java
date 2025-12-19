package lab.zhang.data_science.metrics_mall.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
abstract public class BaseModel {

    Date createTime;

    Date updateTime;


    public void setCreateTimeByTimestamp(long timestamp) {
        this.createTime = new Date(timestamp);
    }

    public long getCreateTimeInTimestamp() {
        if (this.createTime == null) {
            return 0;
        }
        return this.createTime.getTime();
    }

    public void setUpdateTimeByTimestamp(long timestamp) {
        this.updateTime = new Date(timestamp);
    }

    public long getUpdateTimeInTimestamp() {
        if (this.createTime == null) {
            return 0;
        }
        return this.updateTime.getTime();
    }
}
