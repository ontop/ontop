package it.unibz.inf.ontop.iq;

/**
 * TODO: explain
 *
 * IMPORTANT: An IQTreeCache instance CAN ONLY be attached to a SINGLE IQTree!
 */
public interface IQTreeCache {

    boolean isNormalizedForOptimization();

    boolean areDistinctAlreadyRemoved();

    void declareAsNormalizedForOptimizationWithoutEffect();
    IQTreeCache declareAsNormalizedForOptimizationWithEffect();

    /**
     * TODO: consider if we should keep track of the constraint
     */
    IQTreeCache declareConstraintPushedDownWithEffect();

    void declareDistinctRemovalWithoutEffect();
    IQTreeCache declareDistinctRemovalWithEffect();

}
