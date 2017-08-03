package it.unibz.inf.ontop.answering.reformulation.input.translation;

import it.unibz.inf.ontop.datalog.InternalSparqlQuery;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public interface RDF4JInputQueryTranslator extends InputQueryTranslator {

    /**
     * TODO: return an IntermediateQuery instead!
     *
     * TODO: support bindings
     */
    InternalSparqlQuery translate(ParsedQuery inputParsedQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException;
}
