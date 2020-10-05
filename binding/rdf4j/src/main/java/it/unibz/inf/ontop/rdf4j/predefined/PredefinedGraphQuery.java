package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JConstructQuery;

import java.util.Optional;

public interface PredefinedGraphQuery extends PredefinedQuery<RDF4JConstructQuery> {

    Optional<String> getJsonLdFrame();

    /**
     * NB: remains independent from the bindings
     */
    ConstructTemplate getConstructTemplate();
}
