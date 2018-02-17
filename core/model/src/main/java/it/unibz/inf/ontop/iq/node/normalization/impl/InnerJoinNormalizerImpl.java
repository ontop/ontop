package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer.AscendingSubstitutionNormalization;
import it.unibz.inf.ontop.iq.node.normalization.InnerJoinNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class InnerJoinNormalizerImpl implements InnerJoinNormalizer {

    private static final int MAX_ITERATIONS = 100000;
    private final JoinLikeChildBindingLifter bindingLift;
    private final IntermediateQueryFactory iqFactory;
    private final AscendingSubstitutionNormalizer substitutionNormalizer;

    @Inject
    private InnerJoinNormalizerImpl(JoinLikeChildBindingLifter bindingLift, IntermediateQueryFactory iqFactory,
                                    AscendingSubstitutionNormalizer substitutionNormalizer) {
        this.bindingLift = bindingLift;
        this.iqFactory = iqFactory;
        this.substitutionNormalizer = substitutionNormalizer;
    }

    @Override
    public IQTree normalizeForOptimization(InnerJoinNode innerJoinNode, ImmutableList<IQTree> children,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        // Non-final
        State state = new State(children, innerJoinNode.getOptionalFilterCondition(), variableGenerator);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            state = state.liftChildBinding();
            if (state.hasConverged())
                return state.createNormalizedTree(currentIQProperties);
        }

        throw new MinorOntopInternalBugException("InnerJoin.liftBinding() did not converge after " + MAX_ITERATIONS);
    }

    private static ImmutableSet<Variable> extractProjectedVariables(ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class State {
        private final ImmutableSet<Variable> projectedVariables;
        // Parent first
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final ImmutableList<IQTree> children;
        private final Optional<ImmutableExpression> joiningCondition;
        private final boolean hasConverged;
        private final VariableGenerator variableGenerator;

        private State(ImmutableSet<Variable> projectedVariables,
                      ImmutableList<UnaryOperatorNode> ancestors, ImmutableList<IQTree> children,
                      Optional<ImmutableExpression> joiningCondition, VariableGenerator variableGenerator,
                      boolean hasConverged) {
            this.projectedVariables = projectedVariables;
            this.ancestors = ancestors;
            this.children = children;
            this.joiningCondition = joiningCondition;
            this.variableGenerator = variableGenerator;
            this.hasConverged = hasConverged;
        }

        /**
         * Initial constructor
         */
        public State(ImmutableList<IQTree> children, Optional<ImmutableExpression> joiningCondition,
                     VariableGenerator variableGenerator) {
            this(extractProjectedVariables(children), ImmutableList.of(), children,
                    joiningCondition, variableGenerator, false);
        }

        private State updateChildren(ImmutableList<IQTree> newChildren, boolean hasConverged) {
            return new State(projectedVariables, ancestors, newChildren, joiningCondition, variableGenerator, hasConverged);
        }

        private State updateConditionAndChildren(Optional<ImmutableExpression> newCondition,
                                                 ImmutableList<IQTree> newChildren) {
            return new State(projectedVariables, ancestors, newChildren, newCondition, variableGenerator, hasConverged);
        }

        private State updateParentConditionAndChildren(ConstructionNode newParent, Optional<ImmutableExpression> newCondition,
                                                       ImmutableList<IQTree> newChildren) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new State(projectedVariables, newAncestors, newChildren, newCondition, variableGenerator, false);
        }

        /**
         * No child is interpreted as EMPTY
         */
        private State declareAsEmpty() {
            return new State(projectedVariables, ImmutableList.of(), ImmutableList.of(),
                    Optional.empty(), variableGenerator, true);
        }

        /**
         * Lifts the binding OF AT MOST ONE child
         *
         * TODO: refactor
         */
        public State liftChildBinding() {
            ImmutableList<IQTree> liftedChildren = children.stream()
                    .map(c -> c.normalizeForOptimization(variableGenerator))
                    .filter(c -> !(c.getRootNode() instanceof TrueNode))
                    .collect(ImmutableCollectors.toList());

            if (liftedChildren.stream()
                    .anyMatch(IQTree::isDeclaredAsEmpty))
                return declareAsEmpty();


            OptionalInt optionalSelectedLiftedChildPosition = IntStream.range(0, liftedChildren.size())
                    .filter(i -> liftedChildren.get(i).getRootNode() instanceof ConstructionNode)
                    .findFirst();

            /*
             * No substitution to lift -> converged
             */
            if (!optionalSelectedLiftedChildPosition.isPresent())
                return updateChildren(liftedChildren, true);

            int selectedChildPosition = optionalSelectedLiftedChildPosition.getAsInt();
            UnaryIQTree selectedLiftedChild = (UnaryIQTree) liftedChildren.get(selectedChildPosition);

            ConstructionNode selectedChildConstructionNode = (ConstructionNode) selectedLiftedChild.getRootNode();
            IQTree selectedGrandChild = selectedLiftedChild.getChild();

            try {
                return bindingLift.liftRegularChildBinding(selectedChildConstructionNode,
                        selectedChildPosition,
                        selectedGrandChild,
                        liftedChildren, ImmutableSet.of(), joiningCondition, variableGenerator,
                        this::convertIntoState);
            } catch (UnsatisfiableConditionException e) {
                return declareAsEmpty();
            }
        }

        private State convertIntoState(
                ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                Optional<ImmutableExpression> newCondition, ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {

            AscendingSubstitutionNormalization normalization = substitutionNormalizer
                    .normalizeAscendingSubstitution(ascendingSubstitution, extractProjectedVariables(liftedChildren));

            Optional<ConstructionNode> newParent = normalization.generateTopConstructionNode();

            ImmutableList<IQTree> newChildren = IntStream.range(0, liftedChildren.size())
                    .boxed()
                    .map(i -> i == selectedChildPosition
                            ? selectedGrandChild.applyDescendingSubstitution(descendingSubstitution, newCondition)
                            : liftedChildren.get(i).applyDescendingSubstitution(descendingSubstitution, newCondition))
                    .map(normalization::normalizeChild)
                    .collect(ImmutableCollectors.toList());

            return newParent
                    .map(p -> updateParentConditionAndChildren(p, newCondition, newChildren))
                    .orElseGet(() -> updateConditionAndChildren(newCondition, newChildren));
        }

        public IQTree createNormalizedTree(IQProperties currentIQProperties) {
            IQProperties normalizedIQProperties = currentIQProperties.declareNormalizedForOptimization();

            IQTree joinLevelTree = createJoinOrFilterOrEmpty(normalizedIQProperties);

            if (joinLevelTree.isDeclaredAsEmpty())
                return joinLevelTree;

            IQTree ancestorTree = ancestors.stream()
                    .reduce(joinLevelTree, (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            // Should not be called
                            (t1, t2) -> {
                                throw new MinorOntopInternalBugException("The order must be respected");
                            });

            IQTree nonNormalizedTree = ancestorTree.getVariables().equals(projectedVariables)
                    ? ancestorTree
                    : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(projectedVariables), ancestorTree);

            // Normalizes the ancestors (recursive)
            return nonNormalizedTree.normalizeForOptimization(variableGenerator);
        }


        private IQTree createJoinOrFilterOrEmpty(IQProperties normalizedIQProperties) {
            switch (children.size()) {
                // Internal convention for emptiness
                case 0:
                    return iqFactory.createEmptyNode(projectedVariables);
                case 1:
                    IQTree uniqueChild = children.get(0);
                    return joiningCondition
                            .map(e -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(e), uniqueChild))
                            .orElse(uniqueChild);
                default:
                    InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(joiningCondition);
                    return iqFactory.createNaryIQTree(newJoinNode, children,
                            normalizedIQProperties);
            }
        }

        public boolean hasConverged() {
            return hasConverged;
        }
    }

}
