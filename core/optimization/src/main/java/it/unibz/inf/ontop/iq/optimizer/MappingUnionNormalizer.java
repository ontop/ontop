package it.unibz.inf.ontop.iq.optimizer;

/**
 * Lifts unions above projections, until a fixpoint is reached.
 * Then merges consecutive unions and projections.
 *
 * This normalization may be needed for datalog-based mapping optimizers.
 */
public interface MappingUnionNormalizer extends IQOptimizer{
}
