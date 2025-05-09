package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.iq.visit.NormalizationProcedure;
import it.unibz.inf.ontop.iq.visit.impl.StateIQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;


@Singleton
public class FilterNormalizerImpl implements FilterNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final ConditionSimplifier conditionSimplifier;
    private final IQTreeTools iqTreeTools;

    @Inject
    private FilterNormalizerImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                 ConditionSimplifier conditionSimplifier, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.conditionSimplifier = conditionSimplifier;
        this.iqTreeTools = iqTreeTools;
    }

    /**
     * TODO: Optimization: lift direct construction and filter nodes before normalizing them
     *  (so as to reduce the recursive pressure)
     */
    @Override
    public IQTree normalizeForOptimization(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        FilterNormalizationProcedure normalizationProcedure = new FilterNormalizationProcedure(initialFilterNode, initialChild, variableGenerator, treeCache);
        return normalizationProcedure.normalize();
    }

    protected class FilterNormalizationProcedure implements NormalizationProcedure<FilterNormalizationProcedure.State> {

        private final FilterNode initialFilterNode;
        private final IQTree initialChild;
        private final ImmutableSet<Variable> projectedVariables;
        private final VariableGenerator variableGenerator;
        private final IQTreeCache treeCache;

        protected FilterNormalizationProcedure(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            this.initialFilterNode = initialFilterNode;
            this.initialChild = initialChild;
            this.projectedVariables = initialChild.getVariables();
            this.variableGenerator = variableGenerator;
            this.treeCache = treeCache;
        }

        public State getInitialState() {
            return new State(
                        UnaryOperatorSequence.of(),
                        Optional.of(initialFilterNode.getFilterCondition()),
                        initialChild)
                    .normalizeChild();
        }


        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        protected class State extends StateIQVisitor<State> {
            // Composed of ConstructionNode and DistinctNode only
            private final UnaryOperatorSequence<UnaryOperatorNode> ancestors;
            // FilterNode above the child
            private final Optional<ImmutableExpression> condition;
            private final IQTree child;

            protected State(UnaryOperatorSequence<UnaryOperatorNode> ancestors,
                            Optional<ImmutableExpression> condition,
                            IQTree child) {
                this.ancestors = ancestors;
                this.condition = condition;
                this.child = child;
            }

            @Override
            public State transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                var optionalCondition = condition
                        .map(e -> node.getSubstitution().apply(e));
                return new State(ancestors.append(node), optionalCondition, newChild)
                        .continueTo(newChild);
            }

            @Override
            public State transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                return new State(ancestors.append(node), condition, newChild)
                        .continueTo(newChild);
            }

            @Override
            public State transformFilter(UnaryIQTree tree, FilterNode node, IQTree newChild) {
                var optionalCondition = condition
                        .map(c -> termFactory.getConjunction(c, node.getFilterCondition()));
                return new State(ancestors, optionalCondition, newChild)
                        .continueTo(newChild);
            }

            @Override
            public State transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList<IQTree> children) {
                if (condition.isPresent()) {
                    ImmutableExpression newJoiningCondition = node.getOptionalFilterCondition()
                            .map(c -> termFactory.getConjunction(condition.get(), c))
                            .orElse(condition.get());

                    IQTree newChild = iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(newJoiningCondition),
                            children);
                    return new State(ancestors, Optional.empty(), newChild)
                            .done();
                }
                return done();
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof State) {
                    State other = (State) o;
                    return child.equals(other.child);
                }
                return false;
            }

            /**
             * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
             */
            @Override
            public IQTree toIQTree() {

                if (child.isDeclaredAsEmpty())
                    return iqFactory.createEmptyNode(projectedVariables);

                IQTree filterLevelTree = condition
                        .map(iqFactory::createFilterNode)
                        .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, child, treeCache.declareAsNormalizedForOptimizationWithEffect()))
                        .orElse(child);

                if (ancestors.isEmpty())
                    return filterLevelTree;

                return iqTreeTools.createAncestorsUnaryIQTree(ancestors, filterLevelTree)
                        // Normalizes the ancestors (recursive)
                        .normalizeForOptimization(variableGenerator);
            }

            @Override
            public State reduce() {
                return continueTo(child)
                        .normalizeChild()
                        .simplifyAndPropagateDownConstraint();
            }

            private State normalizeChild() {
                return new State(ancestors, condition, child.normalizeForOptimization(variableGenerator));
            }

            private State simplifyAndPropagateDownConstraint() {
                if (condition.isEmpty())
                    return this;

                try {
                    VariableNullability childVariableNullability = child.getVariableNullability();

                    // TODO: also consider the constraint for simplifying the condition
                    ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                            condition.get(), ImmutableList.of(child), childVariableNullability);

                    Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(Optional.empty(),
                            conditionSimplificationResults, childVariableNullability);

                    IQTree newChild = Optional.of(conditionSimplificationResults.getSubstitution())
                            .filter(s -> !s.isEmpty())
                            .map(s -> child.applyDescendingSubstitution(s, downConstraint, variableGenerator))
                            .orElseGet(() -> downConstraint
                                    .map(c -> child.propagateDownConstraint(c, variableGenerator))
                                    .orElse(child));

                    Optional<ConstructionNode> parentConstructionNode = Optional.of(conditionSimplificationResults.getSubstitution())
                            .filter(s -> !s.isEmpty())
                            .map(s -> iqFactory.createConstructionNode(child.getVariables(), s));

                    Optional<ImmutableExpression> newCondition = conditionSimplificationResults.getOptionalExpression();
                    return new State(ancestors.append(parentConstructionNode), newCondition, newChild);
                }
                catch (UnsatisfiableConditionException e) {
                    return new State(UnaryOperatorSequence.of(), Optional.empty(),
                            iqFactory.createEmptyNode(projectedVariables));
                }
            }
        }
    }
}
