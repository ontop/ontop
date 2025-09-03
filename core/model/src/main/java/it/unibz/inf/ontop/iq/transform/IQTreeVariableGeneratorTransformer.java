package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.function.Function;

@FunctionalInterface
public interface IQTreeVariableGeneratorTransformer {
    IQTree transform(IQTree tree, VariableGenerator variableGenerator);

    static IQTreeVariableGeneratorTransformer of(Function<VariableGenerator, IQVisitor<IQTree>> constructor) {
        return (t, vg) -> t.acceptVisitor(constructor.apply(vg));
    }

    static IQTreeVariableGeneratorTransformer of(IQVisitor<IQTree> visitor) {
        return (t, vg) -> t.acceptVisitor(visitor);
    }

    static IQTreeVariableGeneratorTransformer of(IQTreeVariableGeneratorTransformer... transformers) {
        return new CompositeIQTreeVariableGeneratorTransformer(transformers);
    }

}
