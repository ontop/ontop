package it.unibz.inf.ontop.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class OntopEndpointApplication
{

    public static void main(String[] args) {
        SpringApplication.run(OntopEndpointApplication.class, args);
    }


    @Bean
    EndpointConfig endpointConfig(@Value("${ontology}") String owlFile,
                                  @Value("${mapping}") String mappingFile,
                                  @Value("${properties}") String propertiesFile) {
        return new EndpointConfig(owlFile, mappingFile, propertiesFile);
    }


    @Component
    public class CustomizationPortBean implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

        @Value("${port:8080}")
        private int port;

        @Override
        public void customize(ConfigurableServletWebServerFactory server) {
            server.setPort(this.port);
        }
    }

}
