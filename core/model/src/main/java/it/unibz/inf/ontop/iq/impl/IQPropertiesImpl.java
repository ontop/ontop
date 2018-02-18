package it.unibz.inf.ontop.iq.impl;

import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;

public class IQPropertiesImpl implements IQProperties {

    private final boolean isNormalized, areDistinctAlreadyRemoved;

    @AssistedInject
    private IQPropertiesImpl() {
        this.isNormalized = false;
        this.areDistinctAlreadyRemoved = false;
    }

    private IQPropertiesImpl(boolean isNormalized, boolean areDistinctAlreadyRemoved) {
        this.isNormalized = isNormalized;
        this.areDistinctAlreadyRemoved = areDistinctAlreadyRemoved;
    }

    @Override
    public boolean isNormalizedForOptimization() {
        return isNormalized;
    }

    @Override
    public boolean areDistinctAlreadyRemoved() {
        return areDistinctAlreadyRemoved;
    }

    @Override
    public IQProperties declareNormalizedForOptimization() {
        return new IQPropertiesImpl(true, areDistinctAlreadyRemoved);
    }

    @Override
    public IQProperties declareDistinctRemovalWithoutEffect() {
        return new IQPropertiesImpl(isNormalized, true);
    }

    @Override
    public IQProperties declareDistinctRemovalWithEffect() {
        return new IQPropertiesImpl(false, true);
    }
}
