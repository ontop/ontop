package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.iq.IQTreeCache;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class IQTreeCacheImpl implements IQTreeCache {

    @Nonnull
    private boolean isNormalizedForOptimization;
    @Nonnull
    private boolean areDistinctAlreadyRemoved;

    /**
     * Initial constructor
     */
    @Inject
    protected IQTreeCacheImpl() {
        this.isNormalizedForOptimization = false;
        this.areDistinctAlreadyRemoved = false;
    }

    protected IQTreeCacheImpl(boolean isNormalizedForOptimization, boolean areDistinctAlreadyRemoved) {
        this.isNormalizedForOptimization = isNormalizedForOptimization;
        this.areDistinctAlreadyRemoved = areDistinctAlreadyRemoved;
    }

    @Override
    public boolean isNormalizedForOptimization() {
        return isNormalizedForOptimization;
    }

    @Override
    public boolean areDistinctAlreadyRemoved() {
        return areDistinctAlreadyRemoved;
    }

    @Override
    public void declareAsNormalizedForOptimizationWithoutEffect() {
        this.isNormalizedForOptimization = true;
    }

    /**
     * TODO: explicit assumptions about the effects of normalization
     */
    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithEffect() {
        return new IQTreeCacheImpl(true, areDistinctAlreadyRemoved);
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareConstraintPushedDownWithEffect() {
        return new IQTreeCacheImpl(false, areDistinctAlreadyRemoved);
    }

    @Override
    public void declareDistinctRemovalWithoutEffect() {
        this.areDistinctAlreadyRemoved = true;
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareDistinctRemovalWithEffect() {
        return new IQTreeCacheImpl(false, true);
    }
}
