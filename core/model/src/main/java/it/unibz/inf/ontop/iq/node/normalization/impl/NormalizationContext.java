package it.unibz.inf.ontop.iq.node.normalization.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class NormalizationContext {
    protected final VariableGenerator variableGenerator;

    public NormalizationContext(VariableGenerator variableGenerator) {
        this.variableGenerator = variableGenerator;
    }

    protected IQTree normalizeChild(IQTree child) {
        return child.normalizeForOptimization(variableGenerator);
    }

    protected <T extends UnaryOperatorNode> IQTree asIQTree(UnaryOperatorSequence<T> ancestors, IQTree childTree, IQTreeTools iqTreeTools) {
        if (ancestors.isEmpty())
            return childTree;

        return iqTreeTools.unaryIQTreeBuilder()
                .append(ancestors)
                .build(childTree)
                // Normalizes the ancestors (recursive)
                .normalizeForOptimization(variableGenerator);
    }

    protected <T extends UnaryOperatorNode> UnarySubTree<T> normalizeChild(UnarySubTree<T> subTree) {
        return normalizedUnarySubTreeOf(subTree.getOptionalNode(), subTree.getChild());
    }

    protected <T extends UnaryOperatorNode> UnarySubTree<T> normalizedUnarySubTreeOf(Optional<T> optionalNode, IQTree child) {
        return UnarySubTree.of(optionalNode, normalizeChild(child));
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class UnarySubTree<T extends UnaryOperatorNode> {
        private final Optional<T> optionalNode;
        private final IQTree child;

        private UnarySubTree(Optional<T> optionalNode, IQTree child) {
            this.optionalNode = optionalNode;
            this.child = child;
        }

        public static <T extends UnaryOperatorNode> UnarySubTree<T> of(Optional<T> optionalNode, IQTree child) {
            return new UnarySubTree<>(optionalNode, child);
        }

        IQTree getChild() {
            return child;
        }

        Optional<T> getOptionalNode() {
            return optionalNode;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof UnarySubTree) {
                UnarySubTree<?> other = (UnarySubTree<?>) o;
                return optionalNode.equals(other.optionalNode)
                        && child.equals(other.child);
            }
            return false;
        }
    }

    protected static class State<T extends UnaryOperatorNode, S> {
        private final UnaryOperatorSequence<T> ancestors;
        private final S subTree;

        public State(UnaryOperatorSequence<T> ancestors, S subTree) {
            this.ancestors = ancestors;
            this.subTree = subTree;
        }

        public static <T extends UnaryOperatorNode, S> State<T, S> initial(S subTree) {
            return new State<>(UnaryOperatorSequence.of(), subTree);
        }

        public State<T, S> replace(S subTree) {
            return new State<>(ancestors, subTree);
        }

        public State<T, S> lift(T node, S subTree) {
            return new State<>(ancestors.append(node), subTree);
        }

        public State<T, S> lift(Optional<? extends T> node, S subTree) {
            return new State<>(ancestors.append(node), subTree);
        }

        public UnaryOperatorSequence<T> getAncestors() {
            return ancestors;
        }

        public S getSubTree() {
            return subTree;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof State) {
                State<?, ?> other = (State<?, ?>) o;
                return ancestors.equals(other.ancestors)
                        && subTree.equals(other.subTree);
            }
            return false;
        }

        public State<T, S> reachFinal(Function<State<T, S>, Optional<State<T, S>>> transformer) {
            //Non-final
            State<T, S> state = this;
            while (true) {
                Optional<State<T, S>> next = transformer.apply(state);
                if (next.isEmpty())
                    return state;
                state = next.get();
            }
        }

        public State<T, S> reachFixedPoint(Function<State<T, S>, State<T, S>> transformer, int maxIterations) {
            //Non-final
            State<T, S> state = this;
            for(int i = 0; i < maxIterations; i++) {
                State<T, S> next = transformer.apply(state);
                if (next.equals(state))
                    return state;
                state = next;
            }
            throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", maxIterations));
        }
    }
}
