package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;

/**
 * TODO: explain
 *
 * TODO: should we create two sub-interfaces: GeneralOptimizer and GoalOrientedOptimizer?
 * For the moment, we expect the Optimizer to be general, not goal-oriented.
 */
public interface IntermediateQueryOptimizer {

    /**
     * TODO: explain
     */
    IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException;
}
