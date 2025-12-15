package lab.zhang.data_science.metrics_mall.constant;

/**
 * API path constants.
 *
 * @author Rongjin Zhang
 * 
 */
public final class ApiPathConstant {

    /**
     * API base path prefix.
     */
    public static final String API_BASE = "/api";

    /**
     * API version 1 path.
     */
    public static final String API_V1 = API_BASE + "/v1";

    /**
     * Private constructor to prevent instantiation.
     */
    private ApiPathConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

