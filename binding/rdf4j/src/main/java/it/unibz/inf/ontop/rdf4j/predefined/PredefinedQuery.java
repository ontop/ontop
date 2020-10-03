package it.unibz.inf.ontop.rdf4j.predefined;

import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public interface PredefinedQuery {

    String getId();

    ParsedQuery getTupleOrBooleanParsedQuery();

    Optional<String> getName();
    Optional<String> getDescription();

}
