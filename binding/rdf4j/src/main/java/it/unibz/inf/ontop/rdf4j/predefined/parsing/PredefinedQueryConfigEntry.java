package it.unibz.inf.ontop.rdf4j.predefined.parsing;

import org.eclipse.rdf4j.query.Query;

import java.util.Optional;

public interface PredefinedQueryConfigEntry {

    Optional<String> getName();
    Optional<String> getDescription();

    Query.QueryType getQueryType();

    interface QueryParameter {
        Optional<String> getDescription();

        Boolean getSafeForRandomGeneration();

        Boolean getRequired();
    }
}
