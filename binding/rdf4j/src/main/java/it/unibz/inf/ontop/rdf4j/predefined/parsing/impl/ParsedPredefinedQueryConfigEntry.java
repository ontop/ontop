package it.unibz.inf.ontop.rdf4j.predefined.parsing.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Query;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry.QueryParameterCategory.IRI;
import static it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry.QueryParameterCategory.TYPED_LITERAL;

/**
 * TODO: enforce required (shall we use @JsonCreator?)
 */
public class ParsedPredefinedQueryConfigEntry implements PredefinedQueryConfigEntry {
    @JsonProperty(value = "queryType", required = true)
    private String queryTypeString;

    // LAZY
    private Query.QueryType queryType;

    @JsonProperty(value = "name", required = false)
    private String name;
    @JsonProperty(value = "description", required = false)
    private String description;
    @JsonProperty(value = "frame", required = false)
    private Map<String, Object> frame;
    @JsonProperty(value = "parameters", required = true)
    private Map<String, ParsedQueryParameter> parameters;
    // LAZY
    private ImmutableMap<String, QueryParameter> typedParameters;

    @JsonProperty(value = "return404IfEmpty", required = false)
    private Boolean return404IfEmpty;

    @JsonProperty(value = "resultStreaming", required = false)
    private Boolean resultStreaming;

    @Override
    public Query.QueryType getQueryType() {
        if (queryType == null)
            queryType = Query.QueryType.valueOf(queryTypeString.toUpperCase());
        return queryType;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<Map<String, Object>> getFrame() {
        return Optional.ofNullable(frame);
    }

    @Override
    public ImmutableMap<String, QueryParameter> getParameters() {
        if (typedParameters == null)
            typedParameters = ImmutableMap.copyOf(parameters);
        return typedParameters;
    }

    @Override
    public boolean shouldReturn404IfEmpty() {
        if (return404IfEmpty == null)
            return404IfEmpty = false;
        return return404IfEmpty;
    }

    @Override
    public boolean isResultStreamingEnabled() {
        if (resultStreaming == null)
            resultStreaming = false;
        return resultStreaming;
    }

    /**
     * TODO: enforce required (shall we use @JsonCreator?)
     */
    protected static class ParsedQueryParameter implements QueryParameter {
        @JsonProperty(value = "description", required = false)
        private String description;

        @JsonProperty(value = "type", required = true)
        private String type;
        // LAZY
        private QueryParameterType parameterType;

        @JsonProperty(value = "safeForRandomGeneration", required = true)
        private Boolean safeForRandomGeneration;
        @JsonProperty(value = "required", required = true)
        private Boolean required;

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Boolean isSafeForRandomGeneration() {
            return safeForRandomGeneration;
        }

        @Override
        public Boolean getRequired() {
            return required;
        }

        /**
         * TODO: see how to complain about invalid type
         */
        @Override
        public QueryParameterType getType() {
            if (parameterType == null) {
                if (type.toUpperCase().equals("IRI"))
                    return new QueryParameterTypeImpl(IRI);

                String typeString = type.startsWith("xsd:")
                        ? XSD.PREFIX + type.substring(4)
                        : type;
                // TODO: throw a proper exception for invalid IRI
                return new QueryParameterTypeImpl(TYPED_LITERAL, SimpleValueFactory.getInstance().createIRI(typeString));
            }
            return parameterType;
        }
    }
}
