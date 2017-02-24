package it.unibz.inf.ontop.answering.input.translation;

import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.owlrefplatform.core.translator.InternalSparqlQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public interface RDF4JInputQueryTranslator extends InputQueryTranslator {

    /**
     * TODO: return an IntermediateQuery instead!
     *
     * TODO: support bindings
     */
    InternalSparqlQuery translate(ParsedQuery inputParsedQuery) throws OntopUnsupportedInputQueryException;
}
