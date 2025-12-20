package lab.zhang.data_science.metrics_mall.pojo.dto;

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
}
