package lab.zhang.data_science.metrics_mall.constant;

public class HttpConst {

    /**
     * Trace ID parameter name
     */
    public static final String TRACE_ID_PARAM = "traceId";


    /**
     * Trace ID header name
     */
    public static final String TRACE_ID = "X-Request-Id";

    /**
     * Tenant ID header name
     */
    public static final String X_TENANT_ID = "X-Tenant-Id";

    /**
     * User Agent header name
     */
    public static final String USER_AGENT = "User-Agent";


    /**
     * Private constructor to prevent instantiation.
     */
    private HttpConst() {
        throw new UnsupportedOperationException(ErrorMsgConst.UTILITY_CLASS_CANNOT_BE_INSTANTIATED);
    }
}
