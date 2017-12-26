package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.iq.IQProperties;

public class IQPropertiesImpl implements IQProperties {

    private final boolean isLifted;

    protected IQPropertiesImpl() {
        this.isLifted = false;
    }

    private IQPropertiesImpl(boolean isLifted) {
        this.isLifted = false;
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
