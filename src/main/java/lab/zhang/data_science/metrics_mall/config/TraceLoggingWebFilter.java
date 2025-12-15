package lab.zhang.data_science.metrics_mall.config;

import lab.zhang.data_science.metrics_mall.util.LoggingContextUtil;
import lab.zhang.data_science.metrics_mall.util.TraceIdExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

/**
 * Logging filter for trace ID management (WebFlux reactive environment)
 * Sets traceId in MDC for all requests and clears it after request completion
 *
 * @author Rongjin Zhang
 */
@Order(-100)
@Slf4j
public class TraceLoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String paramValue = request.getQueryParams().getFirst(TraceIdExtractor.getTraceIdParam());
        String headerValue = request.getHeaders().getFirst(TraceIdExtractor.getTraceIdHeader());
        BigInteger traceId = TraceIdExtractor.extractTraceId(paramValue, headerValue);
        LoggingContextUtil.setTraceId(traceId);

        return chain.filter(exchange)
                .doFinally(signalType -> LoggingContextUtil.clear());
    }
}

