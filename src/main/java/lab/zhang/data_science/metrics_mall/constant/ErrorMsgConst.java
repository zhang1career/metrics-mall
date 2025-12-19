package lab.zhang.data_science.metrics_mall.constant;

public class ErrorMsgConst {
    public static final String UTILITY_CLASS_CANNOT_BE_INSTANTIATED = "Utility class cannot be instantiated";


    /**
     * Private constructor to prevent instantiation.
     */
    private ErrorMsgConst() {
        throw new UnsupportedOperationException(UTILITY_CLASS_CANNOT_BE_INSTANTIATED);
    }
}
