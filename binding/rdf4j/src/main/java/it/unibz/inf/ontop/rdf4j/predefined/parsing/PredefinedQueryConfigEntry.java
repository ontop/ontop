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

    Optional<Map<String, Object>> getFrame();

    ImmutableMap<String, QueryParameter> getParameters();

    interface QueryParameter {

        Optional<String> getDescription();

        Boolean isSafeForRandomGeneration();

        Boolean getRequired();

        QueryParameterType getType();

        /**
         * Returns a reference value only if safe for random generation
         */
        Optional<String> getReferenceValue(String value);
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
