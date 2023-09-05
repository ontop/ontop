package it.unibz.inf.ontop.iq.optimizer;

/**
 * Merges large complex conjunctions of disjunctions of equalities:
 * - A disjunction of equalities is split into a set of `IN` calls.
 * - A conjunction or disjunction of `IN` calls is merged together if they share the same comparison term.
 */
public interface DisjunctionOfEqualitiesMergingSimplifier extends IQOptimizer {
}
