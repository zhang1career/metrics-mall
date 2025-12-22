package lab.zhang.data_science.metrics_mall.pojo.dto;

import lab.zhang.data_science.metrics_mall.enums.OpEventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;
import java.util.Date;

/**
 * Operation log entity.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OpLogDTO extends BaseDTO {

    private BigInteger id;

    private OpEventEnum event;

    private Long operatorId;

    private Date operateTime;
}

