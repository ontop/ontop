package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.model.OBDAStatement;

/**
 * OBDAStatement specific to Ontop.
 *
 * This interface gives access to inner steps of the SPARQL answering process for analytical purposes.
 * Also provides some benchmarking information.
 *
 */
public interface OntopStatement extends OBDAStatement {

    int getTupleCount(String userQuery) throws OntopQueryAnsweringException;

    String getRewriting(String userQuery) throws OntopReformulationException, OntopInvalidInputQueryException;

    ExecutableQuery getExecutableQuery(String userQuery)
            throws OntopReformulationException, OntopInvalidInputQueryException;
}
