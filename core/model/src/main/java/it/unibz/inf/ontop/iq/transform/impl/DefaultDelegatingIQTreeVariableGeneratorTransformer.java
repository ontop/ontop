package it.unibz.inf.ontop.iq.transform.impl;

import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;

public class DefaultDelegatingIQTreeVariableGeneratorTransformer extends AbstractDelegatingIQTreeVariableGeneratorTransformer {
    private final IQTreeVariableGeneratorTransformer transformer;

    public DefaultDelegatingIQTreeVariableGeneratorTransformer(IQTreeTransformer transformer) {
        this.transformer = reduceDelegation(IQTreeVariableGeneratorTransformer.of2(transformer));
    }

    public DefaultDelegatingIQTreeVariableGeneratorTransformer(IQTreeVariableGeneratorTransformer... transformers) {
        IQTreeVariableGeneratorTransformer[] reducedTransformers = new IQTreeVariableGeneratorTransformer[transformers.length];
        for (int i = 0; i < reducedTransformers.length; i++)
            reducedTransformers[i] = reduceDelegation(transformers[i]);

        this.transformer = IQTreeVariableGeneratorTransformer.of(reducedTransformers);
    }

    private IQTreeVariableGeneratorTransformer reduceDelegation(IQTreeVariableGeneratorTransformer transformer) {
        if (transformer instanceof AbstractDelegatingIQTreeVariableGeneratorTransformer)
            return ((AbstractDelegatingIQTreeVariableGeneratorTransformer)transformer).getTransformer();

        return transformer;
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}
