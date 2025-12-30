package lab.zhang.data_science.metrics_mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat configuration
 * Allows all hosts to support container-to-container communication
 * Note: This works together with server.tomcat.remoteip.host-header configuration in application.yml
 *
 * @author Rongjin Zhang
 */
@Configuration
@Slf4j
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            // Disable strict host validation by setting allowedHosts to empty
            // This allows all hosts to connect
            connector.setProperty("allowedHosts", "");
            log.info("[tomcat] configured to allow all hosts for container-to-container communication");
        });
    }
}

