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

            this.projectedVariables = projectedVariables(initialLeftChild, initialRightChild).immutableCopy();
        }

        IQTree normalize() {
            // Non-final
            State<UnaryOperatorNode, LeftJoinSubTree> state = State.initial(
                    new LeftJoinSubTree(ljNode.getOptionalFilterCondition(),
                            initialLeftChild,
                            initialRightChild));

            // The left child cannot be made empty because of the LJ. Therefore this step is enough to detect emptiness.
            state = liftLeftChild(state);
            if (isEmpty(state.getSubTree()))
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

        State<UnaryOperatorNode, LeftJoinSubTree> next(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            // A DISTINCT on the left might have been waiting because of a not-yet distinct right child
            return liftLeftChild(
                    liftRightChild(
                            normalizeRightChild(
                                    optimizeLeftJoinCondition(
                                            checkRightChildContribution(state)))));
        }

        boolean isToBeReplacedByLeftChild(LeftJoinSubTree subTree) {
            return subTree.rightChild.isDeclaredAsEmpty();
        }

        ConstructionNode createConstructionNode(LeftJoinSubTree subTree, Substitution<? extends ImmutableTerm> substitution) {
            var childrenVariables = projectedVariables(subTree.leftChild, subTree.rightChild).immutableCopy();
            return iqFactory.createConstructionNode(childrenVariables, substitution);
        }

        State<UnaryOperatorNode, LeftJoinSubTree> normalizeLeftChild(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            return state.replace(new LeftJoinSubTree(subTree.ljCondition, normalizeChild(subTree.leftChild), subTree.rightChild));
        }

        State<UnaryOperatorNode, LeftJoinSubTree> liftLeftChild(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            return IQStateOptionalTransformer.reachFinalState(
                    normalizeLeftChild(state),
                    this::liftLeftChildStep);
        }

        Optional<State<UnaryOperatorNode, LeftJoinSubTree>> liftLeftChildStep(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            if (isToBeReplacedByLeftChild(state.getSubTree()))
                return Optional.empty();

            return state.getSubTree().leftChild.acceptVisitor(new LiftLeftChildStep(state))
                    .map(this::normalizeLeftChild);
        }

        private class LiftLeftChildStep extends IQStateOptionalTransformer<State<UnaryOperatorNode, LeftJoinSubTree>> {
            private final State<UnaryOperatorNode, LeftJoinSubTree> state;
            private final LeftJoinSubTree subTree;

            private LiftLeftChildStep(State<UnaryOperatorNode, LeftJoinSubTree> state) {
                this.state = state;
                this.subTree = state.getSubTree();
            }

            Optional<State<UnaryOperatorNode, LeftJoinSubTree>> leftLift(UnaryOperatorNode parent, Optional<ImmutableExpression> newLJCondition, IQTree newLeftChild, IQTree newRightChild) {
                return Optional.of(state.lift(parent, new LeftJoinSubTree(newLJCondition, newLeftChild, newRightChild)));
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformConstruction(UnaryIQTree liftedLeftChild, ConstructionNode leftConstructionNode, IQTree leftGrandChild) {
                try {
                    var bindingLift = bindingLifter.liftRegularChildBinding(
                            leftConstructionNode,
                            0,
                            ImmutableList.of(subTree.leftChild, subTree.rightChild),
                            leftGrandChild.getVariables(),
                            subTree.ljCondition,
                            variableGenerator,
                            variableNullabilityTools.getChildrenVariableNullability(
                                    ImmutableList.of(leftGrandChild, subTree.rightChild)));

                    IQTree rightSubTree = subTree.rightChild.applyDescendingSubstitution(bindingLift.getDescendingSubstitution(), bindingLift.getCondition(), variableGenerator);

                    ImmutableSet<Variable> leftVariables = projectedVariables(subTree.leftChild, leftGrandChild).immutableCopy();

                    Substitution<ImmutableTerm> naiveAscendingSubstitution = bindingLift.getAscendingSubstitution();
                    OptionalRightProvenance rightProvenance = new OptionalRightProvenance(rightSubTree, naiveAscendingSubstitution, leftVariables);

                    Optional<Variable> defaultProvenanceVariable = rightProvenance.getProvenanceVariable();
                    Substitution<ImmutableTerm> ascendingSubstitution =
                            naiveAscendingSubstitution.builder()
                                    .transformOrRetain(v -> !leftVariables.contains(v) ? v : null,
                                            (t, v) -> transformRightSubstitutionValue(t, leftVariables, defaultProvenanceVariable))
                                    .build();

                    return Optional.of(leftLift(createConstructionNode(subTree, ascendingSubstitution), bindingLift.getCondition(), leftGrandChild, rightProvenance.getRightTree()).get());
                }
                catch (UnsatisfiableConditionException e) {
                    // Replaces the LJ by the left child and stops recursion!
                    return leftLift(
                            createConstructionNode(subTree, leftConstructionNode.getSubstitution()),
                            Optional.empty(),
                            leftGrandChild,
                            iqFactory.createEmptyNode(subTree.rightChild.getVariables()));
                }
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree liftedLeftChild, DistinctNode distinctNode, IQTree leftGrandChild) {

                if (isLJDistinctGivenThatLeftChildIsDistinct())
                    return leftLift(distinctNode, subTree.ljCondition, leftGrandChild, subTree.rightChild.removeDistincts());

                return done();
            }

            /**
             * When the left is distinct, isDistinct() behaves like for inner joins
             */
            boolean isLJDistinctGivenThatLeftChildIsDistinct() {
                if (subTree.rightChild.isDistinct())
                    return true;

                IQTree innerJoinTree = iqTreeTools.createInnerJoinTree(
                        subTree.ljCondition,
                        ImmutableList.of(subTree.leftChild, subTree.rightChild));

                return innerJoinTree.isDistinct();
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree liftedLeftChild, FilterNode filterNode, IQTree leftGrandChild) {
                return leftLift(filterNode, subTree.ljCondition, leftGrandChild, subTree.rightChild);
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree liftedLeftChild, InnerJoinNode joinNode, ImmutableList<IQTree> leftGrandChildren) {
                Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                if (joinCondition.isPresent()) {
                    NaryIQTree newLeftChild = iqTreeTools.createInnerJoinTree(leftGrandChildren);
                    // lifts the filter from the join, but stops recursion on the next iteration
                    return leftLift(iqFactory.createFilterNode(joinCondition.get()), subTree.ljCondition, newLeftChild, subTree.rightChild);
                }
                return done();
            }
        }

        private class LiftRightChildStep extends IQStateOptionalTransformer<State<UnaryOperatorNode, LeftJoinSubTree>> {

            private final State<UnaryOperatorNode, LeftJoinSubTree> state;
            private final LeftJoinSubTree subTree;

            private LiftRightChildStep(State<UnaryOperatorNode, LeftJoinSubTree> state) {
                this.state = state;
                this.subTree = state.getSubTree();
            }

            Optional<State<UnaryOperatorNode, LeftJoinSubTree>> rightLift(State<UnaryOperatorNode, LeftJoinSubTree> state, Optional<? extends UnaryOperatorNode> parent, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
                return Optional.of(state.lift(parent, new LeftJoinSubTree(newLJCondition, subTree.leftChild, newRightChild)));
            }

            Optional<State<UnaryOperatorNode, LeftJoinSubTree>> rightLift(State<UnaryOperatorNode, LeftJoinSubTree> state, UnaryOperatorNode parent, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
                return Optional.of(state.lift(parent, new LeftJoinSubTree(newLJCondition, subTree.leftChild, newRightChild)));
            }

            Optional<State<UnaryOperatorNode, LeftJoinSubTree>> rightLift(State<UnaryOperatorNode, LeftJoinSubTree> state, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
                return Optional.of(state.replace(new LeftJoinSubTree(newLJCondition, subTree.leftChild, newRightChild)));
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode constructionNode, IQTree rightGrandChild) {
                Substitution<ImmutableTerm> rightSubstitution = constructionNode.getSubstitution();
                if (rightGrandChild instanceof TrueNode) {
                    Substitution<ImmutableTerm> liftableSubstitution = subTree.ljCondition
                            .map(c -> rightSubstitution.<ImmutableTerm>transform(t -> termFactory.getIfElseNull(c, t)))
                            .orElse(rightSubstitution);

                    return rightLift(state, createConstructionNode(subTree, liftableSubstitution), subTree.ljCondition, rightGrandChild);
                }

                if (rightSubstitution.isEmpty()) {
                    return rightLift(state, createConstructionNode(subTree, rightSubstitution), subTree.ljCondition, rightGrandChild);
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

                ImmutableSet<Variable> leftVariables = subTree.leftChild.getVariables();
                Optional<ImmutableExpression> notOptimizedLJCondition = termFactory.getConjunction(
                        subTree.ljCondition.map(selectedSubstitution::apply),
                        selectedSubstitution.builder()
                                .restrictDomainTo(leftVariables)
                                .toStream(termFactory::getStrictEquality));

                // TODO: only create a right provenance when really needed
                OptionalRightProvenance rightProvenance = provenanceVariable
                        .map(v -> new OptionalRightProvenance(v, rightGrandChild, selectedSubstitution, leftVariables, rightChildRequiredVariables))
                        .orElseGet(() -> new OptionalRightProvenance(rightGrandChild, selectedSubstitution, leftVariables, rightChildRequiredVariables));

                // Tree where a fresh non-nullable variable may have been introduced for the provenance
                return rightLift(state, createConstructionNode(subTree, rightProvenance.computeLiftableSubstitution()),
                        notOptimizedLJCondition, rightProvenance.getRightTree());
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandChild) {
                if (subTree.leftChild.isDistinct())
                    return Optional.of(state.lift(distinctNode, new LeftJoinSubTree(subTree.ljCondition, subTree.leftChild.removeDistincts(), rightGrandChild)));

                return done();
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree rightGrandChild) {
                ImmutableExpression newLJCondition = iqTreeTools.getConjunction(subTree.ljCondition, filterNode.getFilterCondition());
                return rightLift(state, Optional.of(newLJCondition), rightGrandChild);
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> grandChildren) {
                Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                if (joinCondition.isPresent()) {
                    ImmutableExpression newLJCondition = iqTreeTools.getConjunction(subTree.ljCondition, joinCondition.get());

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
            Optional<State<UnaryOperatorNode, LeftJoinSubTree>> liftRightGrandChildWithProvenance(
                    State<UnaryOperatorNode, LeftJoinSubTree> state,
                    Variable provenanceVariable,
                    ImmutableSet<Variable> rightChildRequiredVariables,
                    IQTree rightGrandChild) {

                LeftJoinSubTree subTree = state.getSubTree();

                // Parent construction node: in case some variables where projected out by the right construction node
                Optional<ConstructionNode> optionalProjectingAwayParent =
                        rightChildRequiredVariables.equals(rightGrandChild.getVariables())
                                ? Optional.empty()
                                : Optional.of(createConstructionNode(subTree, substitutionFactory.getSubstitution()));

                return rightGrandChild.acceptVisitor(new IQStateOptionalTransformer<>() {
                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandGrandChild) {
                        if (subTree.leftChild.isDistinct()) {
                            IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                    rightGrandGrandChild, rightGrandChild.getVariables());

                            return rightLift(state, optionalProjectingAwayParent, subTree.ljCondition, newRightChild)
                                    .map(s -> s.lift(
                                            distinctNode, new LeftJoinSubTree(s.getSubTree().ljCondition, s.getSubTree().leftChild.removeDistincts(), s.getSubTree().rightChild)));
                        }
                        return Optional.empty();
                    }
                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree rightGrandGrandChild) {
                        ImmutableExpression filterCondition = filterNode.getFilterCondition();

                        IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                rightGrandGrandChild, Sets.union(rightChildRequiredVariables, filterCondition.getVariables()));

                        return rightLift(state, optionalProjectingAwayParent,
                                Optional.of(iqTreeTools.getConjunction(subTree.ljCondition, filterCondition)),
                                newRightChild);
                    }
                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> grandGrandChildren) {
                        Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                        if (joinCondition.isPresent()) {
                            NaryIQTree newRightGrandChild = iqTreeTools.createInnerJoinTree(grandGrandChildren);

                            IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                    newRightGrandChild, Sets.union(rightChildRequiredVariables, joinCondition.get().getVariables()));

                            return rightLift(state, optionalProjectingAwayParent,
                                    Optional.of(iqTreeTools.getConjunction(subTree.ljCondition, joinCondition.get())),
                                    newRightChild);
                        }
                        return Optional.empty();
                    }
                });
            }
        }

        private State<UnaryOperatorNode, LeftJoinSubTree> optimizeLeftJoinCondition(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (subTree.ljCondition.isEmpty())
                return state;

            try {
                ConditionSimplifier.ExpressionAndSubstitution simplificationResults = conditionSimplifier.simplifyCondition(
                        subTree.ljCondition, subTree.leftChild.getVariables(), ImmutableList.of(subTree.rightChild),
                        variableNullabilityTools.getChildrenVariableNullability(ImmutableList.of(subTree.leftChild, subTree.rightChild)));

                Substitution<? extends VariableOrGroundTerm> downSubstitution =
                        simplificationResults.getSubstitution()
                                .restrictDomainTo(subTree.rightChild.getVariables());

                var optionalCondition = simplificationResults.getOptionalExpression();
                if (downSubstitution.isEmpty()) {
                    if (subTree.ljCondition.equals(optionalCondition))
                        return state;

                    return state.replace(new LeftJoinSubTree(optionalCondition, subTree.leftChild, subTree.rightChild));
                }

                IQTree updatedRightChild = subTree.rightChild.applyDescendingSubstitution(
                        downSubstitution, optionalCondition, variableGenerator);

                var rightProvenance = new OptionalRightProvenance(
                        updatedRightChild, downSubstitution, subTree.leftChild.getVariables());

                return state.lift(createConstructionNode(subTree, rightProvenance.computeLiftableSubstitution()),
                        new LeftJoinSubTree(optionalCondition, subTree.leftChild, rightProvenance.getRightTree()));
            }
            catch (UnsatisfiableConditionException e) {
                return state.replace(new LeftJoinSubTree(Optional.empty(), subTree.leftChild, iqFactory.createEmptyNode(subTree.rightChild.getVariables())));
            }
        }

        private State<UnaryOperatorNode, LeftJoinSubTree> normalizeRightChild(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            return state.replace(new LeftJoinSubTree(subTree.ljCondition, subTree.leftChild, normalizeChild(subTree.rightChild)));
        }

        private State<UnaryOperatorNode, LeftJoinSubTree> liftRightChild(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.getSubTree().rightChild.acceptVisitor(new LiftRightChildStep(state))
                    .orElse(state);
        }


        public boolean isEmpty(LeftJoinSubTree subTree) {
            return subTree.leftChild.isDeclaredAsEmpty();
        }

        protected IQTree asIQTree(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (isEmpty(subTree))
                return iqFactory.createEmptyNode(projectedVariables);

            IQTreeCache normalizedProperties = treeCache.declareAsNormalizedForOptimizationWithEffect();

            IQTree ljLevelTree;
            if (isToBeReplacedByLeftChild(subTree)) {
                Set<Variable> rightSpecificVariables = rightSpecificVariables(subTree.leftChild, subTree.rightChild);

                var newParent = createConstructionNode(subTree,
                        rightSpecificVariables.stream()
                                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));

                ljLevelTree = iqFactory.createUnaryIQTree(newParent, subTree.leftChild, normalizedProperties);
            }
            else if (subTree.rightChild instanceof TrueNode) {
                ljLevelTree = state.getSubTree().leftChild;
            }
            else {
                ljLevelTree = iqFactory.createBinaryNonCommutativeIQTree(
                        iqFactory.createLeftJoinNode(subTree.ljCondition), subTree.leftChild, subTree.rightChild, normalizedProperties);
            }

            IQTree nonNormalizedTree = iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                    .append(state.getAncestors())
                    .build(ljLevelTree);

            // Normalizes the ancestors (recursive)
            return nonNormalizedTree.normalizeForOptimization(variableGenerator);
        }


        public State<UnaryOperatorNode, LeftJoinSubTree> propagateDownLJCondition(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            DownPropagation dc = new DownPropagation(subTree.ljCondition, ImmutableSet.of());
            IQTree newRightChild = dc.propagate(subTree.rightChild, variableGenerator);
            return state.replace(new LeftJoinSubTree(subTree.ljCondition, subTree.leftChild, newRightChild));
        }

        /**
         * If the right child does not contribute new variables and does not change the cardinality,
         * we can drop it
         */
        public State<UnaryOperatorNode, LeftJoinSubTree> checkRightChildContribution(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (Sets.difference(subTree.rightChild.getVariables(), subTree.leftChild.getVariables()).isEmpty()
                    && (!subTree.rightChild.inferUniqueConstraints().isEmpty())) {
                return state.replace(new LeftJoinSubTree(Optional.empty(), subTree.leftChild, iqFactory.createTrueNode()));
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
