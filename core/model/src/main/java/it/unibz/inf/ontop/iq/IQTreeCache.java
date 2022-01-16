package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

/**
 *
 * Certain operations like normalizationForOptimization may produce a new tree, but the latter
 * will always share some properties with the previous one, as this operation preserves the semantics of the tree
 * (same projected variables, etc.). IQTreeCache is designed for caching results about an IQTree and transferring some of them
 * from the previous tree to the new one. In that context, everytime we normalize an IQTree,
 * the new one should use an IQTreeCache that is derived from the previous one, not a fresh new one.
 *
 * WARNING: An IQTreeCache instance can cache additional data structures (e.g. variable nullability, projected variables).
 * It is therefore recommended avoiding re-using it for another tree, as one would need to be very careful.
 *
 * See ConcreteIQTreeCache for more details on which data is stored.
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

    IQTreeCache declareDistinctRemoval(boolean noEffect);

    IQTreeCache applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution);
}
