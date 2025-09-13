package it.unibz.inf.ontop.iq.transform.impl;

import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;

public class CompositeIQTreeVariableGeneratorTransformer extends DelegatingIQTreeVariableGeneratorTransformer {
    private final IQTreeVariableGeneratorTransformer transformer;

    public CompositeIQTreeVariableGeneratorTransformer(IQTreeVariableGeneratorTransformer... transformers) {
        this.transformer = IQTreeVariableGeneratorTransformer.of(transformers);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}
