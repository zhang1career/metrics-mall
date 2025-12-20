package lab.zhang.data_science.metrics_mall.util;

import lab.zhang.data_science.metrics_mall.enums.EnvEnum;
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


    public EnvEnum getEnvEnum() {
        EnvEnum envEnum;
        try {
            envEnum = EnvEnum.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("invalid environment value: {}, use default value instead", environment);
            envEnum = EnvEnum.DEV;
        }
        return envEnum;
    }
}
