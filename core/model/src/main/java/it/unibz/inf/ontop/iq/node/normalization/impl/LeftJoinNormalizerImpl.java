package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.LeftJoinNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer.RightProvenance;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.*;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

@Singleton
public class LeftJoinNormalizerImpl implements LeftJoinNormalizer {

    private static final int MAX_ITERATIONS = 10000;

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ConditionSimplifier conditionSimplifier;
    private final JoinLikeChildBindingLifter bindingLifter;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final IQTreeTools iqTreeTools;

    private final Constant specialProvenanceConstant;

    @Inject
    private LeftJoinNormalizerImpl(CoreSingletons coreSingletons,
                                   ConditionSimplifier conditionSimplifier,
                                   JoinLikeChildBindingLifter bindingLifter,
                                   JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                   RightProvenanceNormalizer rightProvenanceNormalizer) {
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.conditionSimplifier = conditionSimplifier;
        this.bindingLifter = bindingLifter;
        this.variableNullabilityTools = variableNullabilityTools;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.iqTreeTools = coreSingletons.getIQTreeTools();

        this.specialProvenanceConstant = termFactory.getProvenanceSpecialConstant();
    }


    @Override
    public IQTree normalizeForOptimization(LeftJoinNode ljNode, IQTree initialLeftChild, IQTree initialRightChild,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {

        Context context = new Context(ljNode, initialLeftChild, initialRightChild, variableGenerator, treeCache);
        return context.normalize();
    }

    /**
     * A sequence of ConstructionNode and DistinctNode,
     * followed by a LeftJoinNode with two children trees
     */

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class LeftJoinSubTree {
        private final Optional<ImmutableExpression> ljCondition;
        private final IQTree leftChild;
        private final IQTree rightChild;

        private LeftJoinSubTree(Optional<ImmutableExpression> ljCondition, IQTree leftChild, IQTree rightChild) {
            this.ljCondition = ljCondition;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof LeftJoinSubTree) {
                LeftJoinSubTree other = (LeftJoinSubTree)o;
                return ljCondition.equals(other.ljCondition)
                        && leftChild.equals(other.leftChild)
                        && rightChild.equals(other.rightChild);
            }
            return false;
        }
    }

    private class Context extends NormalizationContext {
        private final LeftJoinNode ljNode;
        private final IQTree initialLeftChild;
        private final IQTree initialRightChild;
        private final IQTreeCache treeCache;

        private final ImmutableSet<Variable> projectedVariables;

        private Context(LeftJoinNode ljNode, IQTree initialLeftChild, IQTree initialRightChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.ljNode = ljNode;
            this.initialLeftChild = initialLeftChild;
            this.initialRightChild = initialRightChild;
            this.treeCache = treeCache;

            this.projectedVariables = Stream.of(initialLeftChild, initialRightChild)
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());
        }

        IQTree normalize() {
            // Non-final
            NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state = new NormalizationState2<>(
                    new LeftJoinSubTree(ljNode.getOptionalFilterCondition(),
                            initialLeftChild,
                            initialRightChild));

            // The left child cannot be made empty because of the LJ. Therefore this step is enough to detect emptiness.
            state = liftLeftChild(state);
            if (isEmpty(state))
                return asIQTree(state);

            // Particularly needed when the LJ condition has never been propagated down
            // and no substitution on both side will give an opportunity.
            // TODO: see if it deserves to be in the loop.
            state = propagateDownLJCondition(state);

            return asIQTree(IQStateOptionalTransformer.reachFixedPoint(
                    state,
                    this::next,
                    MAX_ITERATIONS));
        }

        NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> next(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            // A DISTINCT on the left might have been waiting because of a not-yet distinct right child
            return liftLeftChild(
                    liftRightChild(
                            normalizeRightChild(
                                    optimizeLeftJoinCondition(
                                            checkRightChildContribution(state)))));
        }

        boolean isToBeReplacedByLeftChild(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.getSubTree().rightChild.isDeclaredAsEmpty();
        }

        ConstructionNode createConstructionNode(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state, Substitution<? extends ImmutableTerm> substitution) {
            var childrenVariables = projectedVariables(state.getSubTree().leftChild, state.getSubTree().rightChild).immutableCopy();
            return iqFactory.createConstructionNode(childrenVariables, substitution);
        }

        NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> normalizeLeftChild(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.of(new LeftJoinSubTree(state.getSubTree().ljCondition, state.getSubTree().leftChild.normalizeForOptimization(variableGenerator), state.getSubTree().rightChild));
        }

        NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> liftLeftChild(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            return IQStateOptionalTransformer.reachFinalState(
                    normalizeLeftChild(state),
                    this::liftLeftChildStep);
        }

        Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> liftLeftChildStep(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            if (isToBeReplacedByLeftChild(state))
                return Optional.empty();

            return state.getSubTree().leftChild.acceptVisitor(new LiftLeftChildStep(state))
                    .map(this::normalizeLeftChild);
        }

        private class LiftLeftChildStep extends IQStateOptionalTransformer<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> {
            private final NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state;

            private LiftLeftChildStep(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
                this.state = state;
            }

            Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> leftLift(UnaryOperatorNode parent, Optional<ImmutableExpression> newLJCondition, IQTree newLeftChild, IQTree newRightChild) {
                return Optional.of(state.of(parent, new LeftJoinSubTree(newLJCondition, newLeftChild, newRightChild)));
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformConstruction(UnaryIQTree liftedLeftChild, ConstructionNode leftConstructionNode, IQTree leftGrandChild) {
                try {

                    var bindingLift = bindingLifter.liftRegularChildBinding(
                            leftConstructionNode,
                            0,
                            ImmutableList.of(state.getSubTree().leftChild, state.getSubTree().rightChild),
                            leftGrandChild.getVariables(),
                            state.getSubTree().ljCondition,
                            variableGenerator,
                            variableNullabilityTools.getChildrenVariableNullability(
                                    ImmutableList.of(leftGrandChild, state.getSubTree().rightChild)));

                    IQTree rightSubTree = state.getSubTree().rightChild.applyDescendingSubstitution(bindingLift.getDescendingSubstitution(), bindingLift.getCondition(), variableGenerator);

                    ImmutableSet<Variable> leftVariables = projectedVariables(state.getSubTree().leftChild, leftGrandChild).immutableCopy();

                    Substitution<ImmutableTerm> naiveAscendingSubstitution = bindingLift.getAscendingSubstitution();
                    OptionalRightProvenance rightProvenance = new OptionalRightProvenance(rightSubTree, naiveAscendingSubstitution, leftVariables);

                    Optional<Variable> defaultProvenanceVariable = rightProvenance.getProvenanceVariable();
                    Substitution<ImmutableTerm> ascendingSubstitution =
                            naiveAscendingSubstitution.builder()
                                    .transformOrRetain(v -> !leftVariables.contains(v) ? v : null,
                                            (t, v) -> transformRightSubstitutionValue(t, leftVariables, defaultProvenanceVariable))
                                    .build();

                    return Optional.of(leftLift(createConstructionNode(state, ascendingSubstitution), bindingLift.getCondition(), leftGrandChild, rightProvenance.getRightTree()).get());
                }
                catch (UnsatisfiableConditionException e) {
                    // Replaces the LJ by the left child and stops recursion!
                    return leftLift(
                            createConstructionNode(state, leftConstructionNode.getSubstitution()),
                            Optional.empty(),
                            leftGrandChild,
                            iqFactory.createEmptyNode(state.getSubTree().rightChild.getVariables()));
                }
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree liftedLeftChild, DistinctNode distinctNode, IQTree leftGrandChild) {
                if (isLJDistinctGivenThatLeftChildIsDistinct())
                    return leftLift(distinctNode, state.getSubTree().ljCondition, leftGrandChild, state.getSubTree().rightChild.removeDistincts());

                return done();
            }

            /**
             * When the left is distinct, isDistinct() behaves like for inner joins
             */
            boolean isLJDistinctGivenThatLeftChildIsDistinct() {
                if (state.getSubTree().rightChild.isDistinct())
                    return true;

                IQTree innerJoinTree = iqTreeTools.createInnerJoinTree(
                        state.getSubTree().ljCondition,
                        ImmutableList.of(state.getSubTree().leftChild, state.getSubTree().rightChild));

                return innerJoinTree.isDistinct();
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree liftedLeftChild, FilterNode filterNode, IQTree leftGrandChild) {
                return leftLift(filterNode, state.getSubTree().ljCondition, leftGrandChild, state.getSubTree().rightChild);
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree liftedLeftChild, InnerJoinNode joinNode, ImmutableList<IQTree> leftGrandChildren) {
                Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                if (joinCondition.isPresent()) {
                    NaryIQTree newLeftChild = iqTreeTools.createInnerJoinTree(leftGrandChildren);
                    // lifts the filter from the join, but stops recursion on the next iteration
                    return leftLift(iqFactory.createFilterNode(joinCondition.get()), state.getSubTree().ljCondition, newLeftChild, state.getSubTree().rightChild);
                }
                return done();
            }
        }

        private class LiftRightChildStep extends IQStateOptionalTransformer<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> {

            private final NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state;

            private LiftRightChildStep(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
                this.state = state;
            }

            Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> rightLift(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state, Optional<? extends UnaryOperatorNode> parent, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
                return Optional.of(state.of(parent, new LeftJoinSubTree(newLJCondition, state.getSubTree().leftChild, newRightChild)));
            }

            Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> rightLift(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state, UnaryOperatorNode parent, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
                return Optional.of(state.of(parent, new LeftJoinSubTree(newLJCondition, state.getSubTree().leftChild, newRightChild)));
            }

            Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> rightLift(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
                return Optional.of(state.of(new LeftJoinSubTree(newLJCondition, state.getSubTree().leftChild, newRightChild)));
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode constructionNode, IQTree rightGrandChild) {
                Substitution<ImmutableTerm> rightSubstitution = constructionNode.getSubstitution();
                if (rightGrandChild instanceof TrueNode) {
                    Substitution<ImmutableTerm> liftableSubstitution = state.getSubTree().ljCondition
                            .map(c -> rightSubstitution.<ImmutableTerm>transform(t -> termFactory.getIfElseNull(c, t)))
                            .orElse(rightSubstitution);

                    return rightLift(state, createConstructionNode(state, liftableSubstitution), state.getSubTree().ljCondition, rightGrandChild);
                }

                if (rightSubstitution.isEmpty()) {
                    return rightLift(state, createConstructionNode(state, rightSubstitution), state.getSubTree().ljCondition, rightGrandChild);
                }

                Optional<Variable> provenanceVariable = rightSubstitution
                        .getPreImage(t -> t.equals(specialProvenanceConstant))
                        .stream()
                        .findFirst();

                Substitution<ImmutableTerm> selectedSubstitution = provenanceVariable
                        .map(pv -> rightSubstitution.removeFromDomain(ImmutableSet.of(pv)))
                        .orElse(rightSubstitution);

                ImmutableSet<Variable> rightChildRequiredVariables = constructionNode.getChildVariables();
                /*
                 * substitution with only a provenance entry -> see if something can be lifted from the grand child
                 */
                if (selectedSubstitution.isEmpty())
                    return liftRightGrandChildWithProvenance(state,
                            provenanceVariable
                                    .orElseThrow(() -> new MinorOntopInternalBugException("An entry was expected")),
                            rightChildRequiredVariables,
                            rightGrandChild);

                ImmutableSet<Variable> leftVariables = state.getSubTree().leftChild.getVariables();
                Optional<ImmutableExpression> notOptimizedLJCondition = termFactory.getConjunction(
                        state.getSubTree().ljCondition.map(selectedSubstitution::apply),
                        selectedSubstitution.builder()
                                .restrictDomainTo(leftVariables)
                                .toStream(termFactory::getStrictEquality));

                // TODO: only create a right provenance when really needed
                OptionalRightProvenance rightProvenance = provenanceVariable
                        .map(v -> new OptionalRightProvenance(v, rightGrandChild, selectedSubstitution, leftVariables, rightChildRequiredVariables))
                        .orElseGet(() -> new OptionalRightProvenance(rightGrandChild, selectedSubstitution, leftVariables, rightChildRequiredVariables));

                // Tree where a fresh non-nullable variable may have been introduced for the provenance
                return rightLift(state, createConstructionNode(state, rightProvenance.computeLiftableSubstitution()),
                        notOptimizedLJCondition, rightProvenance.getRightTree());
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandChild) {
                if (state.getSubTree().leftChild.isDistinct())
                    return Optional.of(state.of(distinctNode, new LeftJoinSubTree(state.getSubTree().ljCondition, state.getSubTree().leftChild.removeDistincts(), rightGrandChild)));

                return done();
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree rightGrandChild) {
                ImmutableExpression newLJCondition = iqTreeTools.getConjunction(state.getSubTree().ljCondition, filterNode.getFilterCondition());
                return rightLift(state, Optional.of(newLJCondition), rightGrandChild);
            }

            @Override
            public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> grandChildren) {
                Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                if (joinCondition.isPresent()) {
                    ImmutableExpression newLJCondition = iqTreeTools.getConjunction(state.getSubTree().ljCondition, joinCondition.get());

                    NaryIQTree newRightChild = iqTreeTools.createInnerJoinTree(grandChildren);

                    return rightLift(state, Optional.of(newLJCondition), newRightChild);
                }
                return done();
            }

            /**
             * TODO: find a better name
             * <p>
             * When the right child is composed of a construction node with only a provenance entry
             */
            Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> liftRightGrandChildWithProvenance(
                    NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state,
                    Variable provenanceVariable,
                    ImmutableSet<Variable> rightChildRequiredVariables,
                    IQTree rightGrandChild) {

                // Parent construction node: in case some variables where projected out by the right construction node
                Optional<ConstructionNode> optionalProjectingAwayParent =
                        rightChildRequiredVariables.equals(rightGrandChild.getVariables())
                                ? Optional.empty()
                                : Optional.of(createConstructionNode(state, substitutionFactory.getSubstitution()));

                return rightGrandChild.acceptVisitor(new IQStateOptionalTransformer<>() {
                    @Override
                    public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandGrandChild) {
                        if (state.getSubTree().leftChild.isDistinct()) {
                            IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                    rightGrandGrandChild, rightGrandChild.getVariables());

                            return rightLift(state, optionalProjectingAwayParent, state.getSubTree().ljCondition, newRightChild)
                                    .map(s -> s.of(
                                            distinctNode, new LeftJoinSubTree(s.getSubTree().ljCondition, s.getSubTree().leftChild.removeDistincts(), s.getSubTree().rightChild)));
                        }
                        return Optional.empty();
                    }
                    @Override
                    public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree rightGrandGrandChild) {
                        ImmutableExpression filterCondition = filterNode.getFilterCondition();

                        IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                rightGrandGrandChild, Sets.union(rightChildRequiredVariables, filterCondition.getVariables()));

                        return rightLift(state, optionalProjectingAwayParent,
                                Optional.of(iqTreeTools.getConjunction(state.getSubTree().ljCondition, filterCondition)),
                                newRightChild);
                    }
                    @Override
                    public Optional<NormalizationState2<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> grandGrandChildren) {
                        Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                        if (joinCondition.isPresent()) {
                            NaryIQTree newRightGrandChild = iqTreeTools.createInnerJoinTree(grandGrandChildren);

                            IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                    newRightGrandChild, Sets.union(rightChildRequiredVariables, joinCondition.get().getVariables()));

                            return rightLift(state, optionalProjectingAwayParent,
                                    Optional.of(iqTreeTools.getConjunction(state.getSubTree().ljCondition, joinCondition.get())),
                                    newRightChild);
                        }
                        return Optional.empty();
                    }
                });
            }
        }

        private NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> optimizeLeftJoinCondition(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            if (state.getSubTree().ljCondition.isEmpty())
                return state;

            try {
                ConditionSimplifier.ExpressionAndSubstitution simplificationResults = conditionSimplifier.simplifyCondition(
                        state.getSubTree().ljCondition, state.getSubTree().leftChild.getVariables(), ImmutableList.of(state.getSubTree().rightChild),
                        variableNullabilityTools.getChildrenVariableNullability(ImmutableList.of(state.getSubTree().leftChild, state.getSubTree().rightChild)));

                Substitution<? extends VariableOrGroundTerm> downSubstitution =
                        simplificationResults.getSubstitution()
                                .restrictDomainTo(state.getSubTree().rightChild.getVariables());

                var optionalCondition = simplificationResults.getOptionalExpression();
                if (downSubstitution.isEmpty()) {
                    if (state.getSubTree().ljCondition.equals(optionalCondition))
                        return state;

                    return state.of(new LeftJoinSubTree(optionalCondition, state.getSubTree().leftChild, state.getSubTree().rightChild));
                }

                IQTree updatedRightChild = state.getSubTree().rightChild.applyDescendingSubstitution(
                        downSubstitution, optionalCondition, variableGenerator);

                var rightProvenance = new OptionalRightProvenance(
                        updatedRightChild, downSubstitution, state.getSubTree().leftChild.getVariables());

                return state.of(createConstructionNode(state, rightProvenance.computeLiftableSubstitution()),
                        new LeftJoinSubTree(optionalCondition, state.getSubTree().leftChild, rightProvenance.getRightTree()));
            }
            catch (UnsatisfiableConditionException e) {
                return state.of(new LeftJoinSubTree(Optional.empty(), state.getSubTree().leftChild, iqFactory.createEmptyNode(state.getSubTree().rightChild.getVariables())));
            }
        }

        private NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> normalizeRightChild(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.of(new LeftJoinSubTree(state.getSubTree().ljCondition, state.getSubTree().leftChild, state.getSubTree().rightChild.normalizeForOptimization(variableGenerator)));
        }

        private NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> liftRightChild(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.getSubTree().rightChild.acceptVisitor(new LiftRightChildStep(state))
                    .orElse(state);
        }


        public boolean isEmpty(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.getSubTree().leftChild.isDeclaredAsEmpty();
        }

        protected IQTree asIQTree(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            if (isEmpty(state))
                return iqFactory.createEmptyNode(projectedVariables);

            IQTreeCache normalizedProperties = treeCache.declareAsNormalizedForOptimizationWithEffect();

            IQTree ljLevelTree;
            if (isToBeReplacedByLeftChild(state)) {
                Set<Variable> rightSpecificVariables = rightSpecificVariables(state.getSubTree().leftChild, state.getSubTree().rightChild);

                var newParent = createConstructionNode(state,
                        rightSpecificVariables.stream()
                                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));

                ljLevelTree = iqFactory.createUnaryIQTree(newParent, state.getSubTree().leftChild, normalizedProperties);
            }
            else if (state.getSubTree().rightChild instanceof TrueNode) {
                ljLevelTree = state.getSubTree().leftChild;
            }
            else {
                ljLevelTree = iqFactory.createBinaryNonCommutativeIQTree(
                        iqFactory.createLeftJoinNode(state.getSubTree().ljCondition), state.getSubTree().leftChild, state.getSubTree().rightChild, normalizedProperties);
            }

            IQTree nonNormalizedTree = iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                    .append(state.getAncestors())
                    .build(ljLevelTree);

            // Normalizes the ancestors (recursive)
            return nonNormalizedTree.normalizeForOptimization(variableGenerator);
        }


        public NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> propagateDownLJCondition(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            DownPropagation dc = new DownPropagation(state.getSubTree().ljCondition, ImmutableSet.of());
            IQTree newRightChild = dc.propagate(state.getSubTree().rightChild, variableGenerator);
            return state.of(new LeftJoinSubTree(state.getSubTree().ljCondition, state.getSubTree().leftChild, newRightChild));
        }

        /**
         * If the right child does not contribute new variables and does not change the cardinality,
         * we can drop it
         */
        public NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> checkRightChildContribution(NormalizationState2<UnaryOperatorNode, LeftJoinSubTree> state) {
            if (Sets.difference(state.getSubTree().rightChild.getVariables(), state.getSubTree().leftChild.getVariables()).isEmpty()
                    && (!state.getSubTree().rightChild.inferUniqueConstraints().isEmpty())) {
                return state.of(new LeftJoinSubTree(Optional.empty(), state.getSubTree().leftChild, iqFactory.createTrueNode()));
            }
            return state;
        }



        private class OptionalRightProvenance {
            private final IQTree rightTree;
            private final Substitution<? extends ImmutableTerm> selectedSubstitution;
            private final ImmutableSet<Variable> leftVariables;
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            private final Optional<RightProvenance> rightProvenance;

            /**
             * When at least one value does not depend on a right-specific variable
             * (i.e. is a ground term or only depends on left variables)
             * <p>
             * A fresh non-nullable variable may have been introduced for the provenance inside a sparse data node
             * of the returned right tree
             */

            OptionalRightProvenance(IQTree rightTree, Substitution<? extends ImmutableTerm> selectedSubstitution, ImmutableSet<Variable> leftVariables) {
                this(rightTree, selectedSubstitution, leftVariables, rightTree.getVariables());
            }

            OptionalRightProvenance(IQTree rightTree,
                                    Substitution<? extends ImmutableTerm> selectedSubstitution,
                                    ImmutableSet<Variable> leftVariables,
                                    ImmutableSet<Variable> rightRequiredVariables) {
                this.selectedSubstitution = selectedSubstitution;
                this.leftVariables = leftVariables;
                this.rightProvenance = getRightProvenance(rightTree, selectedSubstitution, leftVariables, rightRequiredVariables);
                this.rightTree = rightProvenance
                        .map(RightProvenance::getRightTree)
                        .orElse(rightTree);
            }

            private Optional<RightProvenance> getRightProvenance(IQTree rightTree,
                                                                 Substitution<? extends ImmutableTerm> selectedSubstitution,
                                                                 ImmutableSet<Variable> leftVariables,
                                                                 ImmutableSet<Variable> rightRequiredVariables) {
                if (selectedSubstitution.removeFromDomain(leftVariables)
                        .rangeAnyMatch(t -> needsAnExternalProvenanceVariable(t, leftVariables))) {

                    return Optional.of(rightProvenanceNormalizer.normalizeRightProvenance(
                            rightTree, leftVariables, rightRequiredVariables, variableGenerator));
                }
                return Optional.empty();
            }

            OptionalRightProvenance(Variable provenanceVariable,
                                    IQTree tree,
                                    Substitution<? extends ImmutableTerm> selectedSubstitution,
                                    ImmutableSet<Variable> leftVariables,
                                    ImmutableSet<Variable> treeVariablesToProject) {
                this.selectedSubstitution = selectedSubstitution;
                this.leftVariables = leftVariables;
                this.rightTree = createSubTreeWithProvenance(provenanceVariable, tree, treeVariablesToProject);
                this.rightProvenance = Optional.of(new RightProvenance(provenanceVariable, rightTree));
            }

            Optional<Variable> getProvenanceVariable() {
                return rightProvenance.map(RightProvenance::getProvenanceVariable);
            }

            IQTree getRightTree() {
                return rightTree;
            }

            /**
             * TODO: explain
             * <p>
             * Right provenance variable: always there if needed
             * (when some definitions do not depend on a right-specific variable)
             */
            Substitution<ImmutableTerm> computeLiftableSubstitution() {
                Optional<Variable> rightProvenanceVariable = getProvenanceVariable();
                return selectedSubstitution.builder()
                        .removeFromDomain(leftVariables)
                        .transform(t -> transformRightSubstitutionValue(t, leftVariables, rightProvenanceVariable))
                        .build();
            }
        }
    }

    private UnaryIQTree createSubTreeWithProvenance(Variable provenanceVariable, IQTree tree,
                                                    Set<Variable> treeVariablesToProject) {

        return iqFactory.createUnaryIQTree(
                iqTreeTools.createExtendingConstructionNode(
                        treeVariablesToProject,
                        substitutionFactory.getSubstitution(provenanceVariable, specialProvenanceConstant)),
                tree);
    }

    private ImmutableTerm transformRightSubstitutionValue(ImmutableTerm value,
                                                          ImmutableSet<Variable> leftVariables,
                                                          Optional<Variable> defaultRightProvenanceVariable) {
        if (isNullWhenRightIsRejected(value, leftVariables))
            return value;

        Variable provenanceVariable = Optional.of(value)
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .flatMap(f -> f.proposeProvenanceVariables()
                        .filter(v -> !leftVariables.contains(v))
                        .findAny())
                .or(() -> defaultRightProvenanceVariable)
                .orElseThrow(() -> new MinorOntopInternalBugException("A default provenance variable was needed"));

        return termFactory.getIfElseNull(termFactory.getDBIsNotNull(provenanceVariable), value);
    }



    /**
     * Return true when
     * - the immutable term does NOT become null when the right child is rejected
     * - AND the term is NOT capable of proposing its own provenance variable
     */
    private boolean needsAnExternalProvenanceVariable(ImmutableTerm immutableTerm, ImmutableSet<Variable> leftVariables) {
        if (isNullWhenRightIsRejected(immutableTerm, leftVariables))
            return false;

        if (immutableTerm instanceof ImmutableFunctionalTerm) {
            return ((ImmutableFunctionalTerm) immutableTerm).proposeProvenanceVariables()
                    .allMatch(leftVariables::contains);
        }
        // Variable and constant
        return true;
    }

    /**
     * Return true when the term is guaranteed to be NULL when the right is rejected
     */
    private boolean isNullWhenRightIsRejected(ImmutableTerm immutableTerm, ImmutableSet<Variable> leftVariables) {
        Substitution<ImmutableTerm> nullSubstitution =
                Sets.difference(immutableTerm.getVariableStream().collect(ImmutableCollectors.toSet()), leftVariables).stream()
                        .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()));

        return nullSubstitution.applyToTerm(immutableTerm)
                .simplify()
                .isNull();
    }
}
