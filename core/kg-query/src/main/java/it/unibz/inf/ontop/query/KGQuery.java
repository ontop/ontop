package it.unibz.inf.ontop.query;


import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Must throw an OntopInvalidKGQueryException at CONSTRUCTION time if the query is invalid.
 *
 * At translation time, may throw an OntopUnsupportedInputQueryException
 *
 */
public interface KGQuery<R extends OBDAResultSet> {

    String getOriginalString();

    IQ translate(KGQueryTranslator translator) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException;
}
