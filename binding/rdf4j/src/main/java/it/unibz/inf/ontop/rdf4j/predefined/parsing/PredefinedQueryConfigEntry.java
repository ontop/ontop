package it.unibz.inf.ontop.rdf4j.predefined.parsing;

import com.google.common.collect.ImmutableMap;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.Query;

import java.util.Map;
import java.util.Optional;

public interface PredefinedQueryConfigEntry {

    Optional<String> getName();
    Optional<String> getDescription();

    Query.QueryType getQueryType();

    /**
     * False by default
     */
    boolean shouldReturn404IfEmpty();

    /**
     * False by default
     */
    boolean isResultStreamingEnabled();

    Optional<Map<String, Object>> getFrame();

    ImmutableMap<String, QueryParameter> getParameters();


    interface QueryParameter {

        Optional<String> getDescription();

        Boolean isSafeForRandomGeneration();

        Boolean getRequired();

        QueryParameterType getType();
    }

    interface QueryParameterType {
        QueryParameterCategory getCategory();
        Optional<IRI> getDatatypeIRI();
    }

    enum QueryParameterCategory {
        IRI,
        TYPED_LITERAL
    }

}
