package it.unibz.inf.ontop.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class OntopEndpointApplication {

    public static void main(String[] args) {
        SpringApplication.run(OntopEndpointApplication.class, args);
    }

    @Bean
    WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> portConfig(@Value("${port:8080}") int port) {
        return server -> server.setPort(port);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${cors-allowed-origins:}") String[] allowedOrigins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/*").allowedOrigins(allowedOrigins);
            }
        };
    }
}
