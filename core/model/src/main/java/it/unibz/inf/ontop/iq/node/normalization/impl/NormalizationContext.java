package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class NormalizationContext {
    protected final VariableGenerator variableGenerator;
    protected final ImmutableSet<Variable> projectedVariables;
    protected final IQTreeTools iqTreeTools;

    private final IQTreeCache treeCache;

    public NormalizationContext(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache, IQTreeTools iqTreeTools) {
        this.projectedVariables = projectedVariables;
        this.variableGenerator = variableGenerator;
        this.iqTreeTools = iqTreeTools;
        this.treeCache = treeCache;
    }

    protected IQTree normalizeSubTreeRecursively(IQTree child) {
        return child.normalizeForOptimization(variableGenerator);
    }

    protected IQTreeCache getNormalizedTreeCache(boolean changed) {
        return changed
                ? treeCache.declareAsNormalizedForOptimizationWithEffect()
                : treeCache.declareAsNormalizedForOptimizationWithoutEffect();
    }

    protected EmptyNode createEmptyNode() {
        return iqTreeTools.createEmptyNode(projectedVariables);
    }

    protected <T extends UnaryOperatorNode> IQTree asIQTree(UnaryOperatorSequence<T> ancestors, IQTree childTree) {
        if (ancestors.isEmpty())
            return childTree;

        // Normalizes the ancestors (recursive)
        return normalizeSubTreeRecursively(
                iqTreeTools.unaryIQTreeBuilder()
                        .append(ancestors)
                        .build(childTree));
    }

    protected <T extends UnaryOperatorNode> UnarySubTree<T> normalizeChild(UnarySubTree<T> subTree) {
        return UnarySubTree.of(subTree.getOptionalNode(), normalizeSubTreeRecursively(subTree.getChild()));
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

        public static <T extends UnaryOperatorNode> UnarySubTree<T> of(T node, IQTree child) {
            return new UnarySubTree<>(Optional.of(node), child);
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

        public State<T, S> replace(Function<S, S> subTreeTransformer) {
            return new State<>(ancestors, subTreeTransformer.apply(subTree));
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

        public State<T, S> reachFinal(Function<State<T, S> , State<T, S>> preparator, Function<State<T, S>, Optional<State<T, S>>> transformer) {
            //Non-final
            State<T, S> state = this;
            while (true) {
                State<T, S>  prepared = preparator.apply(state);
                Optional<State<T, S>> next = transformer.apply(prepared);
                if (next.isEmpty())
                    return prepared;
                state = next.get();
            }
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

        public State<T, S> reachFinal(int maxIterations, Function<State<T, S>, Optional<State<T, S>>> transformer) {
            //Non-final
            State<T, S> state = this;
            for (int i = 0; i < maxIterations; i++) {
                Optional<State<T, S>> next = transformer.apply(state);
                if (next.isEmpty())
                    return state;
                state = next.get();
            }
            throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", maxIterations));
        }


        @SafeVarargs
        public final State<T, S> reachFixedPoint(int maxIterations, Function<State<T, S>, State<T, S>>... transformers) {
            //Non-final
            State<T, S> state = this;
            for(int i = 0; i < maxIterations; i++) {
                State<T, S> next = state;
                for (Function<State<T, S>, State<T, S>> transformer : transformers)
                    next = transformer.apply(next);
                if (next.equals(state))
                    return state;
                state = next;
            }
            throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", maxIterations));
        }
    }
}
