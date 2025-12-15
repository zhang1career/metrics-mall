package lab.zhang.data_science.metrics_mall.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lab.zhang.data_science.metrics_mall.util.LoggingContextUtil;
import lab.zhang.data_science.metrics_mall.util.TraceIdExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.math.BigInteger;

/**
 * Logging interceptor for trace ID management (Servlet environment)
 * Sets traceId in MDC for all requests and clears it after request completion
 *
 * @author Rongjin Zhang
 */
@Slf4j
public class TraceLoggingFilter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String paramValue = request.getParameter(TraceIdExtractor.getTraceIdParam());
        String headerValue = request.getHeader(TraceIdExtractor.getTraceIdHeader());
        BigInteger traceId = TraceIdExtractor.extractTraceId(paramValue, headerValue);
        LoggingContextUtil.setTraceId(traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoggingContextUtil.clear();
    }
}