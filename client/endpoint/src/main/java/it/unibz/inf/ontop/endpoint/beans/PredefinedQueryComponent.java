package it.unibz.inf.ontop.endpoint.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.moandjiezana.toml.Toml;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQueryConfig;
import it.unibz.inf.ontop.rdf4j.predefined.impl.FakeOntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.impl.OntopRDF4JPredefinedQueryEngineImpl;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

@Component
public class PredefinedQueryComponent {

    @Bean
    private OntopRDF4JPredefinedQueryEngine setupPredefinedQueryEngine(@Value("${predefined-config:#{null}}") String configFile,
                                                                       @Value("${predefined-queries:#{null}}") String queryFile,
                                                                       @Value("${contexts:#{null}}") String contextFile,
                                                                       OntopVirtualRepository repository) throws IOException {
        if (configFile != null) {
            if (queryFile == null)
                throw new IllegalArgumentException("predefined-queries is expected when predefined-config is provided");

            Toml toml = new Toml().read(new FileReader(queryFile));
            ImmutableMap<String, Object> queryMap = ImmutableMap.copyOf(toml.toMap());

            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(new FileReader(configFile), Config.class);

            // TODO: parse the contexts

            return new OntopRDF4JPredefinedQueryEngineImpl(repository.getOntopEngine(), queryMap, config.queries);
        }
        else
            return new FakeOntopRDF4JPredefinedQueryEngine();
    }

    public static class Config {
        private final ImmutableMap<String, PredefinedQueryConfig.QueryEntry> queries;

        @JsonCreator
        public Config(@JsonProperty("queries") Map<String, PredefinedQueryConfig.QueryEntry> queries) {
            this.queries = ImmutableMap.copyOf(queries);
        }
    }
}
