package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;

import java.util.Optional;

public interface PredefinedQuery<Q extends InputQuery> {

    Q getInputQuery();

    String getId();

    Optional<String> getName();
    Optional<String> getDescription();

}
