package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Performs all the structural and semantic optimizations
 *
 * TODO: find a better name
 */
public interface GeneralStructuralAndSemanticIQOptimizer extends IQOptimizer {

    IQ optimize(IQ query, QueryContext queryContext);
}
