package it.unibz.inf.ontop.answering.connection;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.iq.IQ;

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

    IQ getExecutableQuery(InputQuery inputQuery) throws OntopReformulationException;
}
