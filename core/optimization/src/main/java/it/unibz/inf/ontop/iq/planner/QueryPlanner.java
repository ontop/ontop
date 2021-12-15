package it.unibz.inf.ontop.iq.planner;

import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;

/**
 * By contrast to structural and semantic optimizations, the query planner can explore decisions that
 *  "it might regret afterwards" in isolated branches.
 *
 *  Serious query planners are supposed to take into account statistical information about the data source,
 *  while structural and semantic optimizations do not consider this kind of information.
 *
 */
public interface QueryPlanner extends IQOptimizer {
}
