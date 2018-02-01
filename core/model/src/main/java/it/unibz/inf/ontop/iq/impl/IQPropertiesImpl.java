package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.iq.IQProperties;

public class IQPropertiesImpl implements IQProperties {

    private final boolean isNormalized;

    protected IQPropertiesImpl() {
        this.isNormalized = false;
    }

    private IQPropertiesImpl(boolean isNormalized) {
        this.isNormalized = false;
    }

    @Override
    public boolean isNormalizedForOptimization() {
        return isNormalized;
    }

    @Override
    public IQProperties declareNormalizedForOptimization() {
        return new IQPropertiesImpl(true);
    }
}
