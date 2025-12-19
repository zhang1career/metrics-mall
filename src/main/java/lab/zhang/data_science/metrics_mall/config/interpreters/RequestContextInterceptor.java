package lab.zhang.data_science.metrics_mall.config.interpreters;


import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lab.zhang.data_science.metrics_mall.components.RequestContext;
import lab.zhang.data_science.metrics_mall.util.LoggingContextUtil;
import lab.zhang.data_science.metrics_mall.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.math.BigInteger;

import static lab.zhang.data_science.metrics_mall.constant.HttpConst.*;


@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    @Autowired
    private RequestContext requestContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // trace id
        BigInteger traceId = BigInteger.ZERO;
        String traceIdStr = request.getParameter(TRACE_ID_PARAM);
        if (StrUtil.isBlank(traceIdStr)) {
            traceIdStr = request.getHeader(TRACE_ID);
        }
        if (!StrUtil.isBlank(traceIdStr)) {
            traceId = new BigInteger(traceIdStr);
        }

        // user id
        Long userId = parseUserIdFromToken(request);

        // tenant id
        String tenantId = request.getHeader(X_TENANT_ID);

        // populate request context
        requestContext.setTraceId(traceId);
        requestContext.setUserId(userId);
        requestContext.setTenantId(tenantId);
        requestContext.setStartTs(TimeUtil.getCurrentTime());
        requestContext.setClientIp(request.getRemoteAddr());
        requestContext.setUserAgent(request.getHeader(USER_AGENT));

        // set trace id into logging context
        LoggingContextUtil.setTraceId(traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoggingContextUtil.clear();
    }

    private Long parseUserIdFromToken(HttpServletRequest request) {
        // TODO 解析 JWT / Session / Token
        return 123L;
    }
}
