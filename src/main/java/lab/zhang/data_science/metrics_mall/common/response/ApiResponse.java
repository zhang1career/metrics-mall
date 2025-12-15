package lab.zhang.data_science.metrics_mall.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API response wrapper.
 *
 * @author Rongjin Zhang
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * Response code, 0 indicates success, non-zero indicates failure.
     */
    private Integer code;
    
    /**
     * Response message.
     */
    private String errmsg;
    
    /**
     * Response data.
     */
    private T data;
    
    /**
     * Create success response.
     *
     * @param data response data
     * @param <T>  data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .errmsg("")
                .data(data)
                .build();
    }
    
    /**
     * Create error response.
     *
     * @param code   error code
     * @param errmsg error message
     * @param <T>    data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(Integer code, String errmsg) {
        return ApiResponse.<T>builder()
                .code(code)
                .errmsg(errmsg)
                .data(null)
                .build();
    }
}

