package lab.zhang.data_science.metrics_mall.util;

import cn.hutool.core.util.StrUtil;

import java.math.BigInteger;

/**
 * Trace ID extractor utility
 * Extracts trace ID from request parameters or headers
 * Supports both Servlet and WebFlux environments
 *
 * @author Rongjin Zhang
 */
public class TraceIdExtractor {

    /**
     * Trace ID parameter name
     */
    private static final String TRACE_ID_PARAM = "traceId";

    /**
     * Trace ID header name
     */
    private static final String TRACE_ID_HEADER = "X-Request-Id";

    /**
     * Extract trace ID from parameter first, then from header
     *
     * @param paramValue parameter value (can be null)
     * @param headerValue header value (can be null)
     * @return trace ID as BigInteger, BigInteger.ZERO if not found or invalid
     */
    public static BigInteger extractTraceId(String paramValue, String headerValue) {
        String traceIdStr = paramValue;
        if (StrUtil.isBlank(traceIdStr)) {
            traceIdStr = headerValue;
        }

        if (StrUtil.isBlank(traceIdStr)) {
            return BigInteger.ZERO;
        }

        try {
            return new BigInteger(traceIdStr.trim());
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }

    /**
     * Get trace ID parameter name
     *
     * @return parameter name
     */
    public static String getTraceIdParam() {
        return TRACE_ID_PARAM;
    }

    /**
     * Get trace ID header name
     *
     * @return header name
     */
    public static String getTraceIdHeader() {
        return TRACE_ID_HEADER;
    }
}

