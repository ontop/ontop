package it.unibz.inf.ontop.iq.impl;

import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTreeCache;

public class IQPropertiesImpl implements IQProperties {

    private final boolean isNormalized, areDistinctAlreadyRemoved;
    private final IQTreeCache emptyTreeCache;

    @AssistedInject
    private IQPropertiesImpl(IQTreeCache emptyTreeCache) {
        this.emptyTreeCache = emptyTreeCache;
        this.isNormalized = false;
        this.areDistinctAlreadyRemoved = false;
    }

    private IQPropertiesImpl(IQTreeCache emptyTreeCache, boolean isNormalized, boolean areDistinctAlreadyRemoved) {
        this.emptyTreeCache = emptyTreeCache;
        this.isNormalized = isNormalized;
        this.areDistinctAlreadyRemoved = areDistinctAlreadyRemoved;
    }

    @Override
    public IQProperties declareNormalizedForOptimization() {
        return new IQPropertiesImpl(emptyTreeCache, true, areDistinctAlreadyRemoved);
    }

    @Override
    public IQProperties declareDistinctRemovalWithoutEffect() {
        return new IQPropertiesImpl(emptyTreeCache, isNormalized, true);
    }

    @Override
    public IQTreeCache convertIQTreeCache() {
        //Non-final
        IQTreeCache treeCache = emptyTreeCache;
        if (isNormalized)
            treeCache = treeCache.declareAsNormalizedForOptimizationWithoutEffect();
        if (areDistinctAlreadyRemoved)
            treeCache = treeCache.declareDistinctRemoval(true);
        return treeCache;
    }
}
