package it.unibz.inf.ontop.iq.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;

public class IQPropertiesImpl implements IQProperties {

    private final boolean isNormalized;

    @AssistedInject
    private IQPropertiesImpl() {
        this.isNormalized = false;
    }

    @AssistedInject
    private IQPropertiesImpl(@Assisted boolean isNormalized) {
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
