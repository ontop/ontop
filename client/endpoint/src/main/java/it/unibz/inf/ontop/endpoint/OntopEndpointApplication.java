package it.unibz.inf.ontop.endpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OntopEndpointApplication {

    public static void main(String[] args) {
        SpringApplication.run(OntopEndpointApplication.class, args);
    }

    public static String owlFile;

    public static String  mappingFile;

    public static String propertiesFile;


    @Bean SparqlQueryController queryController(){
        return new SparqlQueryController(owlFile, mappingFile, propertiesFile);
    }


}
