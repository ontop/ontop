package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

@FunctionalInterface
public interface IQTreeVariableGeneratorTransformer {
    IQTree transform(IQTree tree, VariableGenerator variableGenerator);
}
