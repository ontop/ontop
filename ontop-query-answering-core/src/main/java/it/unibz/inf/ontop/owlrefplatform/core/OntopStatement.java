package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.answering.input.InputQuery;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.model.OBDAStatement;

/**
 * OBDAStatement specific to Ontop.
 *
 * This interface gives access to inner steps of the SPARQL answering process for analytical purposes.
 * Also provides some benchmarking information.
 *
 */
public interface OntopStatement extends OBDAStatement {

    int getTupleCount(InputQuery inputQuery) throws OntopReformulationException, OntopQueryEvaluationException, OntopConnectionException;

    String getRewritingRendering(InputQuery inputQuery) throws OntopReformulationException, OntopInvalidInputQueryException;

    ExecutableQuery getExecutableQuery(InputQuery inputQuery) throws OntopReformulationException;
}
