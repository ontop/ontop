package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

/**
 * TODO: explain
 *
 *
 * WARNING: An IQTreeCache instance can cache additional data structures (e.g. variable nullability, projected variables).
 * It is therefore recommended to avoid to re-use it for another tree, as one would need to be very careful.
 */
public interface IQTreeCache {

    boolean isNormalizedForOptimization();

    boolean areDistinctAlreadyRemoved();

    IQTreeCache declareAsNormalizedForOptimizationWithoutEffect();
    IQTreeCache declareAsNormalizedForOptimizationWithEffect();

    /**
     * TODO: consider if we should keep track of the constraint
     */
    IQTreeCache declareConstraintPushedDownWithEffect();

    IQTreeCache declareDistinctRemovalWithoutEffect();
    IQTreeCache declareDistinctRemovalWithEffect();

    IQTreeCache applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution);
}
