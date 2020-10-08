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
    @JsonProperty(value = "context", required = false)
    private Object context;
    @JsonProperty(value = "outputContext", required = false)
    private Object outputContext;
    @JsonProperty(value = "frame", required = false)
    private Map<String, Object> frame;
    @JsonProperty(value = "parameters", required = true)
    private Map<String, ParsedQueryParameter> parameters;
    // LAZY
    private ImmutableMap<String, QueryParameter> typedParameters;

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

    public Optional<Object> getContext() {
        return Optional.ofNullable(context);
    }

    public Optional<Object> getOutputContext() {
        return Optional.ofNullable(outputContext);
    }

    public Optional<Map<String, Object>> getFrame() {
        return Optional.ofNullable(frame);
    }

    @Override
    public ImmutableMap<String, QueryParameter> getParameters() {
        if (typedParameters == null)
            typedParameters = ImmutableMap.copyOf(parameters);
        return typedParameters;
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

        // LAZY
        private String referenceValue;

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

        @Override
        public Optional<String> getReferenceValue(String value) {
            if (!isSafeForRandomGeneration())
                return Optional.empty();

            if (referenceValue == null) {
                synchronized (this) {
                    if (referenceValue == null)
                        referenceValue = generateReferenceValue(getType());
                }
            }
            return Optional.of(referenceValue);
        }

        private static String generateReferenceValue(QueryParameterType paramType) {

            switch (paramType.getCategory()) {
                case IRI:
                    throw new RuntimeException("TODO: support generating ref values for IRIs");
                case TYPED_LITERAL:
                    return generateReferenceLiteralValue(paramType.getDatatypeIRI()
                            .orElseThrow(() -> new MinorOntopInternalBugException("Invalid typed literal")));
                default:
                    throw new MinorOntopInternalBugException("Unexpected parameter type: " + paramType.getCategory());
            }
        }

        private static String generateReferenceLiteralValue(IRI datatype) {
            String iriString = datatype.stringValue();

            // TODO: avoid the series of IF
            if (iriString.equals(XSD.STRING.getIRIString())) {
                return UUID.randomUUID().toString();
            }
            else
                throw new RuntimeException("TODO: support random generation of " + datatype);
        }
    }
}
