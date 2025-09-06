package it.unibz.inf.ontop.iq.transform.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class DelegatingIQTreeVariableGeneratorTransformer implements IQTreeVariableGeneratorTransformer {

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return getTransformer().transform(tree, variableGenerator);
    }

    protected abstract IQTreeVariableGeneratorTransformer getTransformer();
}
