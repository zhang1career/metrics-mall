package lab.zhang.data_science.metrics_mall.config.interpreters;

import lab.zhang.data_science.metrics_mall.components.RequestContext;
import lab.zhang.data_science.metrics_mall.util.LoggingContextUtil;
import lab.zhang.data_science.metrics_mall.util.TimeUtil;
import lab.zhang.data_science.metrics_mall.util.TraceIdExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

import static lab.zhang.data_science.metrics_mall.constant.HttpConst.*;

/**
 * Logging filter for trace ID management (WebFlux reactive environment)
 * Sets traceId in MDC for all requests and clears it after request completion
 *
 * @author Rongjin Zhang
 */
@Order(-100)
@Slf4j
public class RequestContextWebFilter implements WebFilter {

    @Autowired
    private RequestContext requestContext;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // trace id
        ServerHttpRequest request = exchange.getRequest();
        String paramValue = request.getQueryParams().getFirst(TRACE_ID_PARAM);
        String headerValue = request.getHeaders().getFirst(TRACE_ID);
        BigInteger traceId = TraceIdExtractor.extractTraceId(paramValue, headerValue);

        // user id
        Long userId = parseUserIdFromToken(request);

        // tenant id
        String tenantId = request.getHeaders().getFirst(X_TENANT_ID);

        // populate request context
        requestContext.setTraceId(traceId);
        requestContext.setUserId(userId);
        requestContext.setTenantId(tenantId);
        requestContext.setStartTs(TimeUtil.getCurrentTime());
        requestContext.setClientIp(request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : null);
        requestContext.setUserAgent(request.getHeaders().getFirst(USER_AGENT));

        // set trace id into logging context
        LoggingContextUtil.setTraceId(traceId);

        return chain.filter(exchange)
                .doFinally(signalType -> LoggingContextUtil.clear());
    }

    private Long parseUserIdFromToken(ServerHttpRequest request) {
        // TODO 解析 JWT / Session / Token
        return 123L;
    }
}

