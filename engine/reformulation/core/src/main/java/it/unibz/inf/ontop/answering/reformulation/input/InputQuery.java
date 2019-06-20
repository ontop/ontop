package it.unibz.inf.ontop.answering.reformulation.input;


import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Must throw an OntopInvalidInputQueryException at CONSTRUCTION time if the input query is invalid.
 *
 * At translation time, may throw an OntopUnsupportedInputQueryException
 *
 */
public interface InputQuery<R extends OBDAResultSet> {

    String getInputString();

    IQ translate(InputQueryTranslator translator) throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException;
}
