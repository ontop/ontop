package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nullable;

/**
 * Performs all the structural and semantic optimizations
 */
public interface GeneralStructuralAndSemanticIQOptimizer extends IQOptimizer {

    /**
     * When the query context is null, does not apply some optimizations
     */
    IQ optimize(IQ query, @Nullable QueryContext queryContext);

    /**
     * Please use the other method instead.
     * In a future version, this method will be removed (i.e. no more extending IQOptimizer).
     */
    @Deprecated
    @Override
    IQ optimize(IQ query);
}
