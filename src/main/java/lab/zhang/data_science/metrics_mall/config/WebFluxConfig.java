package lab.zhang.data_science.metrics_mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebFlux configuration
 * Note: When both spring-boot-starter-web and spring-boot-starter-webflux are present,
 * RouterFunction beans may not work properly in Servlet environment.
 * Use @RestController with Mono return type instead for reactive endpoints.
 *
 * @author Rongjin Zhang
 */
@Configuration
@Slf4j
public class WebFluxConfig {

    @Bean
    public TraceLoggingWebFilter traceLoggingWebFilter() {
        return new TraceLoggingWebFilter();
    }

    public WebFluxConfig() {
        log.info("[init] reactive endpoints support enabled using @RestController with Mono return type");
    }
}
