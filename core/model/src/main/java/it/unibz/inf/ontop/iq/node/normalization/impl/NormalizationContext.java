package it.unibz.inf.ontop.iq.node.normalization.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class NormalizationContext {
    protected final VariableGenerator variableGenerator;

    public NormalizationContext(VariableGenerator variableGenerator) {
        this.variableGenerator = variableGenerator;
    }

    protected static class NormalizationState2<T extends UnaryOperatorNode, S> {
        private final UnaryOperatorSequence<T> ancestors;
        private final S subTree;

        public NormalizationState2(UnaryOperatorSequence<T> ancestors, S subTree) {
            this.ancestors = ancestors;
            this.subTree = subTree;
        }

        public NormalizationState2(S subTree) {
            this(UnaryOperatorSequence.of(), subTree);
        }

        public NormalizationState2<T, S> of(S subTree) {
            return new NormalizationState2<>(getAncestors(), subTree);
        }

        public NormalizationState2<T, S> of(T node, S subTree) {
            return new NormalizationState2<>(getAncestors().append(node), subTree);
        }

        public NormalizationState2<T, S> of(Optional<? extends T> node, S subTree) {
            return new NormalizationState2<>(getAncestors().append(node), subTree);
        }

        protected UnaryOperatorSequence<T> getAncestors() {
            return ancestors;
        }

        protected S getSubTree() {
            return subTree;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NormalizationState2) {
                NormalizationState2<?, ?> other = (NormalizationState2<?, ?>) o;
                return subTree.equals(other.subTree)
                        && ancestors.equals(other.ancestors);
            }
            return false;
        }

    }

}
