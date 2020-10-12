package it.unibz.inf.ontop.endpoint.beans;

import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQueries;
import it.unibz.inf.ontop.rdf4j.predefined.impl.FakeOntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.impl.OntopRDF4JPredefinedQueryEngineImpl;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryParser;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.impl.PredefinedQueryParserImpl;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;

@Component
public class PredefinedQueryComponent {

    @Bean
    private OntopRDF4JPredefinedQueryEngine setupPredefinedQueryEngine(@Value("${predefined-config:#{null}}") String configFile,
                                                                       @Value("${predefined-queries:#{null}}") String queryFile,
                                                                       @Value("${contexts:#{null}}") String contextFile,
                                                                       OntopVirtualRepository repository,
                                                                       OntopSystemConfiguration configuration) throws IOException {
        if (configFile != null) {
            if (queryFile == null)
                throw new IllegalArgumentException("predefined-queries is expected when predefined-config is provided");

            PredefinedQueryParser parser = new PredefinedQueryParserImpl(configuration);

            FileReader queryReader = new FileReader(queryFile);
            FileReader configReader = new FileReader(configFile);
            PredefinedQueries predefinedQueries =  contextFile == null
                    ? parser.parse(configReader, queryReader)
                    : parser.parse(configReader, queryReader, new FileReader(contextFile));

            return new OntopRDF4JPredefinedQueryEngineImpl(repository.getOntopEngine(), predefinedQueries, configuration);
        }
        else
            return new FakeOntopRDF4JPredefinedQueryEngine();
    }
}
