package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.query.ConstructTemplate;
import it.unibz.inf.ontop.query.RDF4JConstructQuery;

import java.util.Map;
import java.util.Optional;

public interface PredefinedGraphQuery extends PredefinedQuery<RDF4JConstructQuery> {

    Optional<Map<String, Object>> getJsonLdFrame();

    /**
     * NB: remains independent from the bindings
     */
    ConstructTemplate getConstructTemplate();
}
