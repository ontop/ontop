package it.unibz.inf.ontop.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OntopEndpointApplication implements ApplicationRunner  {

    public static void main(String[] args) {
        SpringApplication.run(OntopEndpointApplication.class, args);
    }


    @Bean
    EndpointConfig endpointConfig(@Value("${t}") String owlFile,
                                  @Value("${m}") String mappingFile,
                                  @Value("${p}") String propertiesFile) {
        return new EndpointConfig(owlFile, mappingFile, propertiesFile);
    }

    @Override
    public void run(ApplicationArguments args) {
        args.getOptionNames().forEach(System.out::println);
    }
}
