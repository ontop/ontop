package it.unibz.inf.ontop.iq.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;

public class IQPropertiesImpl implements IQProperties {

    private final boolean isLifted;

    @AssistedInject
    private IQPropertiesImpl() {
        this.isLifted = false;
    }

    @AssistedInject
    private IQPropertiesImpl(@Assisted boolean isLifted) {
        this.isLifted = isLifted;
    }

    @Override
    public boolean isLifted() {
        return isLifted;
    }

    @Override
    public IQProperties declareLifted() {
        return new IQPropertiesImpl(true);
    }
}
