package it.unibz.inf.ontop.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class OntopEndpointApplication {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(OntopEndpointApplication.class, args);
    }

    @Bean
    WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> portConfig(@Value("${port:8080}") int port) {
        return server -> server.setPort(port);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${cors-allowed-origins:#{null}}") String[] allowedOrigins) {
        // System.err.println("Allowed origins: " + Arrays.toString(allowedOrigins));
        return (allowedOrigins == null)
                // No allowed origin, let SpringBoot ignore CORS concerns
                ? new WebMvcConfigurer() {}
                // Otherwise, let SpringBoot react to CORS headers
                : new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins(allowedOrigins);
            }
        };
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(OntopEndpointApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }

}
