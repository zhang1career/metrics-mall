package lab.zhang.data_science.metrics_mall.components;

import lab.zhang.data_science.metrics_mall.enums.OpEventEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.math.BigInteger;

/**
 * Metric snapshot query model for service layer.
 *
 * @author Rongjin Zhang
 */
@Component
@RequestScope
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {

    /**
     * 请求唯一标识，用于日志链路追踪
     */
    private BigInteger traceId;

    /**
     * 租户 ID（多租户系统）
     */
    private String tenantId;

    /**
     * 当前登录用户 ID
     */
    private Long userId;

    /**
     * 请求开始时间（用于耗时统计）
     */
    private long startTs;

    /**
     * 客户端 IP
     */
    private String clientIp;

    /**
     * 请求来源
     */
    private String userAgent;

    /**
     * .事件类型
     */
    private OpEventEnum event;
}

