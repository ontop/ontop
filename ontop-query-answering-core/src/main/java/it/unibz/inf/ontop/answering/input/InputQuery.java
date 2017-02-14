package it.unibz.inf.ontop.answering.input;


import it.unibz.inf.ontop.answering.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.model.OBDAResultSet;
import it.unibz.inf.ontop.owlrefplatform.core.translator.InternalSparqlQuery;

/**
 * Must throw an OntopInvalidInputQueryException at CONSTRUCTION time if the input query is invalid.
 *
 * At translation time, may throw an OntopUnsupportedInputQueryException
 *
 */
public interface InputQuery<R extends OBDAResultSet> {

    /**
     * TODO: return an IntermediateQuery instead!
     */
    InternalSparqlQuery translate(InputQueryTranslator translator) throws OntopUnsupportedInputQueryException;
}
