package lab.zhang.data_science.metrics_mall.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Environment utility class.
 * Note: This class is currently not used but kept for future use.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Component
public class EnvUtil {
    /**
     * Current environment (read from configuration, default is test environment)
     */
    @Value("${metrics_mall.environment:TEST}")
    private String environment;

    /**
     * Get environment string.
     *
     * @return environment string
     */
    public String getEnvironment() {
        return environment;
    }
}
