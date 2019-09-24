package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

/**
 * Performs all the structural and semantic optimizations
 *
 * TODO: find a better name
 */
public interface GeneralStructuralAndSemanticIQOptimizer {

    /**
     * TODO: get rid of the executor registry and inherit IQOptimizer
     */
    IQ optimize(IQ query, ExecutorRegistry executorRegistry);
}
