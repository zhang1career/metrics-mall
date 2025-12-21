package lab.zhang.data_science.metrics_mall.model;

import lab.zhang.data_science.metrics_mall.enums.OpEventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;
import java.util.Date;

/**
 * Operation log model.
 *
 * @author Rongjin Zhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OpLog extends BaseModel {

    /**
     * Operation log id.
     */
    private BigInteger id;

    /**
     * Event type enum.
     */
    private OpEventEnum event;

    /**
     * Operator identifier.
     */
    private Long operatorId;

    /**
     * Operation time.
     */
    private Date operateTime;
}

