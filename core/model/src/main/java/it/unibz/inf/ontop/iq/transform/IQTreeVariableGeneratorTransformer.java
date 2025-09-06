package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface IQTreeVariableGeneratorTransformer {
    IQTree transform(IQTree tree, VariableGenerator variableGenerator);

    static IQTreeVariableGeneratorTransformer of(Function<VariableGenerator, IQVisitor<IQTree>> constructor) {
        return (t, vg) -> t.acceptVisitor(constructor.apply(vg));
    }

    static IQTreeVariableGeneratorTransformer of(IQVisitor<IQTree> visitor) {
        return (t, vg) -> t.acceptVisitor(visitor);
    }

    static IQTreeVariableGeneratorTransformer of2(IQTreeTransformer transformer) {
        return (t, vg) -> transformer.transform(t);
    }

    static IQTreeVariableGeneratorTransformer of(IQTreeVariableGeneratorTransformer... transformers) {
        return new CompositeIQTreeVariableGeneratorTransformer(transformers);
    }

    default IQTreeVariableGeneratorTransformer fixpoint() {
        return (t, vg) -> {
            IQTree prev, tree = t;
            do {
                prev = tree;
                tree = transform(tree, vg);
            } while (!prev.equals(tree));
            return prev;
        };
    }

    default IQTreeVariableGeneratorTransformer fixpoint(int max) {
        return (t, vg) -> {
            IQTree tree = t;
            for (int i = 0; i < max; i++) {
                IQTree prev = tree;
                tree = transform(tree, vg);
                if (prev.equals(tree))
                    return tree;
            }
            throw new MinorOntopInternalBugException("MAX_LOOP reached");
        };
    }
}
