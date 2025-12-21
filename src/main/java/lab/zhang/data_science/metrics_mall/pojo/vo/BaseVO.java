package lab.zhang.data_science.metrics_mall.pojo.vo;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base View Object (VO) class.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class BaseVO {

    private String createTime;

    private String updateTime;
}
