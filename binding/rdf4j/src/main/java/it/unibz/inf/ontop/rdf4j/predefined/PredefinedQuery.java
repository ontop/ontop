package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQuery;

import java.util.Optional;

public interface PredefinedQuery<Q extends RDF4JInputQuery> {

    Q getInputQuery();

    String getId();

    Optional<String> getName();
    Optional<String> getDescription();

}
