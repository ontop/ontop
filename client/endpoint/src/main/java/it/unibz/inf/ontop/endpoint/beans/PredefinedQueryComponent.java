package it.unibz.inf.ontop.endpoint.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.moandjiezana.toml.Toml;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.impl.FakeOntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class PredefinedQueryComponent {

    @Bean
    private OntopRDF4JPredefinedQueryEngine setupPredefinedQueryEngine(@Value("${predefined-config:#{null}}") String configFile,
                                                                       @Value("${predefined-queries:#{null}}") String queryFile,
                                                                       @Value("${contexts:#{null}}") String contextFile,
                                                                       OntopVirtualRepository repository) throws IOException {
        PredefinedQueries predefinedQueries = extractPredefinedQueries(configFile, queryFile, contextFile);
        return new FakeOntopRDF4JPredefinedQueryEngine();
    }

    private PredefinedQueries extractPredefinedQueries(String configFile, String queryFile, String contextFile) throws IOException {
        if (configFile != null) {
            if (queryFile == null)
                throw new IllegalArgumentException("predefined-queries is expected when predefined-config is provided");

            Toml toml = new Toml().read(new FileReader(queryFile));
            ImmutableMap<String, Object> queryMap = ImmutableMap.copyOf(toml.toMap());

            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(new FileReader(configFile), Config.class);

            // TODO: parse the contexts

            return new PredefinedQueries(queryMap, config.queries);
        }
        else
            return new PredefinedQueries(ImmutableMap.of(), ImmutableMap.of());
    }


    public static class PredefinedQueries {
        private final ImmutableMap<String, Object> sparqlQueryMap;
        private final ImmutableMap<String, QueryEntry> queryConfig;

        public PredefinedQueries(ImmutableMap<String, Object> sparqlQueryMap, ImmutableMap<String, QueryEntry> queryConfig) {
            this.sparqlQueryMap = sparqlQueryMap;
            this.queryConfig = queryConfig;
        }

        public Map<String, Object> getSparqlQueryMap() {
            return sparqlQueryMap;
        }

        public ImmutableMap<String, QueryEntry> getQueryConfig() {
            return queryConfig;
        }
    }

    public static class Config {
        private final ImmutableMap<String, QueryEntry> queries;

        @JsonCreator
        public Config(@JsonProperty("queries") Map<String, QueryEntry> queries) {
            this.queries = ImmutableMap.copyOf(queries);
        }
    }

    /**
     * TODO: enforce required (shall we use @JsonCreator?)
     */
    public static class QueryEntry {
        @JsonProperty(value = "sparqlQueryType", required = true)
        private String sparqlQueryType;
        @JsonProperty(value = "name", required = false)
        private String name;
        @JsonProperty(value = "description", required = false)
        private String description;
        @JsonProperty(value = "context", required = false)
        private Object context;
        @JsonProperty(value = "outputContext", required = false)
        private Object outputContext;
        @JsonProperty(value = "frame", required = false)
        private Map<String, Object> frame;
        @JsonProperty(value = "parameters", required = true)
        private Map<String, QueryParameter> parameters;

        public String getSparqlQueryType() {
            return sparqlQueryType;
        }

        public Optional<String> getName() {
            return Optional.ofNullable(name);
        }

        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        public Optional<Object> getContext() {
            return Optional.ofNullable(context);
        }

        public Optional<Object> getOutputContext() {
            return Optional.ofNullable(outputContext);
        }

        public Optional<Map<String, Object>> getFrame() {
            return Optional.ofNullable(frame);
        }

        public Map<String, QueryParameter> getParameters() {
            return parameters;
        }
    }

    /**
     * TODO: enforce required (shall we use @JsonCreator?)
     */
    public static class QueryParameter {
        @JsonProperty(value = "description", required = false)
        private String description;
        @JsonProperty(value = "type", required = true)
        private String type;
        @JsonProperty(value = "safeForRandomGeneration", required = true)
        private Boolean safeForRandomGeneration;
        @JsonProperty(value = "required", required = true)
        private Boolean required;

        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        public String getType() {
            return type;
        }

        public Boolean getSafeForRandomGeneration() {
            return safeForRandomGeneration;
        }

        public Boolean getRequired() {
            return required;
        }
    }

}
