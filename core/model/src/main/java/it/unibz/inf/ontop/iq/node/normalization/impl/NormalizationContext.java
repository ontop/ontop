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

    protected <T extends UnaryOperatorNode> IQTree asIQTree(UnaryOperatorSequence<T> ancestors, IQTree childTree, IQTreeTools iqTreeTools) {
        if (ancestors.isEmpty())
            return childTree;

        return iqTreeTools.unaryIQTreeBuilder()
                .append(ancestors)
                .build(childTree)
                // Normalizes the ancestors (recursive)
                .normalizeForOptimization(variableGenerator);
    }

    protected static class State<T extends UnaryOperatorNode, S> {
        private final UnaryOperatorSequence<T> ancestors;
        private final S subTree;

        public State(UnaryOperatorSequence<T> ancestors, S subTree) {
            this.ancestors = ancestors;
            this.subTree = subTree;
        }

        public State(S subTree) {
            this(UnaryOperatorSequence.of(), subTree);
        }

        public State<T, S> of(S subTree) {
            return new State<>(getAncestors(), subTree);
        }

        public State<T, S> of(T node, S subTree) {
            return new State<>(getAncestors().append(node), subTree);
        }

        public State<T, S> of(Optional<? extends T> node, S subTree) {
            return new State<>(getAncestors().append(node), subTree);
        }

        protected UnaryOperatorSequence<T> getAncestors() {
            return ancestors;
        }

        protected S getSubTree() {
            return subTree;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof State) {
                State<?, ?> other = (State<?, ?>) o;
                return subTree.equals(other.subTree)
                        && ancestors.equals(other.ancestors);
            }
            return false;
        }

        public State<T, S> reachFinalState(Function<State<T, S>, Optional<State<T, S>>> transformer) {
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
