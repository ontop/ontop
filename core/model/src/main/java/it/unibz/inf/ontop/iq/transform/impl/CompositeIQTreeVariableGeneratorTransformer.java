package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class CompositeIQTreeVariableGeneratorTransformer implements IQTreeVariableGeneratorTransformer {
    private final ImmutableList<IQTreeVariableGeneratorTransformer> transformers;

    public CompositeIQTreeVariableGeneratorTransformer(IQTreeVariableGeneratorTransformer... transformers) {
        this.transformers = ImmutableList.copyOf(transformers);
    }

    @Override
    public IQTree transform(IQTree initial, VariableGenerator variableGenerator) {
        // non-final
        IQTree tree = initial;
        for (IQTreeVariableGeneratorTransformer normalizer : transformers) {
            tree = normalizer.transform(tree, variableGenerator);
        }
        return tree;
    }
}
