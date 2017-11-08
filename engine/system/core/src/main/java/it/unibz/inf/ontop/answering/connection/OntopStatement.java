package it.unibz.inf.ontop.answering.connection;

import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.exception.*;

/**
 * OBDAStatement specific to Ontop.
 *
 * This interface gives access to inner steps of the SPARQL answering process for analytical purposes.
 * Also provides some benchmarking information.
 *
 */
public interface OntopStatement extends OBDAStatement {

    int getTupleCount(InputQuery inputQuery) throws OntopReformulationException, OntopQueryEvaluationException, OntopConnectionException;

    String getRewritingRendering(InputQuery inputQuery) throws OntopReformulationException;

    ExecutableQuery getExecutableQuery(InputQuery inputQuery) throws OntopReformulationException;
}
