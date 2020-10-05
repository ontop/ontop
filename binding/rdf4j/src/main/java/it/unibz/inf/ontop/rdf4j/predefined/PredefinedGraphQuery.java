package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;

import java.util.Optional;

public interface PredefinedGraphQuery extends PredefinedQuery<ConstructQuery> {

    Optional<String> getJsonLdFrame();

    ConstructTemplate getConstructTemplate();
}
