package lab.zhang.data_science.metrics_mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 * Supports both Servlet (Tomcat) and WebFlux (Reactive) environments
 * - Servlet: TraceLoggingFilter (HandlerInterceptor) is registered here
 * - WebFlux: TraceLoggingWebFilter (WebFilter) is auto-registered via @Component
 *
 * @author Rongjin Zhang
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public TraceLoggingFilter traceLoggingFilter() {
        return new TraceLoggingFilter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceLoggingFilter())
                .addPathPatterns("/api/**")  // Apply to all API endpoints
                .excludePathPatterns("/api/dicts"); // Exclude dict endpoint if needed
    }
}