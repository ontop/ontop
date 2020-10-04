package it.unibz.inf.ontop.answering.reformulation.input.translation;

import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public interface RDF4JInputQueryTranslator extends InputQueryTranslator {

    IQ translate(ParsedQuery inputParsedQuery, BindingSet bindings)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException;

    IQ translateAskQuery(ParsedQuery parsedQuery, BindingSet bindings)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException;
}
