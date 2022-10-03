package it.unibz.inf.ontop.query.translation;

import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public interface RDF4JInputQueryTranslator extends InputQueryTranslator {

    IQ translate(ParsedQuery inputParsedQuery, BindingSet bindings)
            throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException;

    IQ translateAskQuery(ParsedQuery parsedQuery, BindingSet bindings)
            throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException;
}
