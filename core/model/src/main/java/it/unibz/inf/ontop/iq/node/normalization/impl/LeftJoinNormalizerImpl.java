package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.LeftJoinNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultIQTreeOptionalVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;

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
        LeftJoinSubTree initialSubTree = new LeftJoinSubTree(ljNode.getOptionalFilterCondition(), initialLeftChild, initialRightChild);
        Context context = new Context(initialSubTree.projectedVariables(), variableGenerator, treeCache);
        return context.normalize(initialSubTree);
    }

    /**
     * A state is a sequence of ConstructionNode and DistinctNode,
     * followed by a LeftJoinSubTree (a LeftJoinNode with two children trees)
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

        LeftJoinSubTree replaceRight(Optional<ImmutableExpression> ljCondition, IQTree rightChild) {
            return new LeftJoinSubTree(ljCondition, this.leftChild, rightChild);
        }

        LeftJoinSubTree replaceRight(IQTree rightChild) {
            return new LeftJoinSubTree(this.ljCondition, this.leftChild, rightChild);
        }

        LeftJoinSubTree replaceLeft(IQTree leftChild) {
            return new LeftJoinSubTree(this.ljCondition, leftChild, this.rightChild);
        }

        LeftJoinSubTree replaceChildren(IQTree leftChild, IQTree rightChild) {
            return new LeftJoinSubTree(this.ljCondition, leftChild, rightChild);
        }

        ImmutableSet<Variable> projectedVariables() {
            return BinaryNonCommutativeIQTreeTools.projectedVariables(leftChild, rightChild).immutableCopy();
        }

        Set<Variable> rightSpecificVariables() {
            return BinaryNonCommutativeIQTreeTools.rightSpecificVariables(leftChild, rightChild);
        }

        ImmutableList<IQTree> children() {
            return ImmutableList.of(leftChild, rightChild);
        }

        Optional<ImmutableExpression> ljCondition() {
            return ljCondition;
        }

        IQTree leftChild() {
            return leftChild;
        }

        IQTree rightChild() {
            return rightChild;
        }

        boolean isEmpty() {
            return leftChild.isDeclaredAsEmpty();
        }

        boolean isRightChildEmpty() {
            return rightChild.isDeclaredAsEmpty();
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

        private Context(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(projectedVariables, variableGenerator, treeCache, LeftJoinNormalizerImpl.this.iqTreeTools);
        }

        IQTree normalize(LeftJoinSubTree initialSubTree) {
            // Non-final
            var state = State.initial(initialSubTree);

            // The left child cannot be made empty because of the LJ. Therefore this step is enough to detect emptiness.
            state = liftLeftChild(state);
            if (state.getSubTree().isEmpty())
                return asIQTree(state);

            // Particularly needed when the LJ condition has never been propagated down
            // and no substitution on both side will give an opportunity.
            // TODO: see if it deserves to be in the loop.
            state = propagateDownLJCondition(state);

            state =  state.reachFixedPoint(MAX_ITERATIONS,
                    // A DISTINCT on the left might have been waiting because of a not-yet distinct right child
                    this::checkRightChildContribution,
                    this::optimizeLeftJoinCondition,
                    this::liftRightChild,
                    this::liftLeftChild);

            return asIQTree(state);
        }

        ConstructionNode createConstructionNode(LeftJoinSubTree subTree, Substitution<? extends ImmutableTerm> substitution) {
            return iqFactory.createConstructionNode(subTree.projectedVariables(), substitution);
        }

        EmptyNode createEmptyRightChild(LeftJoinSubTree subTree) {
            return iqFactory.createEmptyNode(subTree.rightChild().getVariables());
        }

        State<UnaryOperatorNode, LeftJoinSubTree> liftLeftChild(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            return state.replace(t -> t.replaceLeft(normalizeSubTreeRecursively(t.leftChild())))
                    .reachFinal(this::liftLeftChildStep);
        }

        /**
         * One-step lifting of CONSTRUCT, DISTINCT and FILTER form the left child of LEFT JOIN.
         * The joining condition of INNER JOIN is also lifted, which terminates lifting
         * (on the next iteration).
         * The child is assumed to be normalized, so repeated applications are possible
         * (without the need to normalize the child again).
         */

        Optional<State<UnaryOperatorNode, LeftJoinSubTree>> liftLeftChildStep(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (subTree.isRightChildEmpty()) // can result from lifting a CONSTRUCT
                return Optional.empty();

            return subTree.leftChild().acceptVisitor(new DefaultIQTreeOptionalVisitingTransformer<>() {

                @Override
                public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformConstruction(UnaryIQTree liftedLeftChild, ConstructionNode constructionNode, IQTree leftGrandChild) {
                    try {
                        var bindingLift = bindingLifter.liftRegularChildBinding(
                                constructionNode,
                                0,
                                subTree.children(),
                                leftGrandChild.getVariables(),
                                subTree.ljCondition(),
                                variableGenerator,
                                variableNullabilityTools.getChildrenVariableNullability(
                                        ImmutableList.of(leftGrandChild, subTree.rightChild())));

                        DownPropagation dp = iqTreeTools.createDownPropagation(
                                bindingLift.getDescendingSubstitution(),
                                bindingLift.getCondition(),
                                subTree.rightChild().getVariables(),
                                variableGenerator);

                        IQTree rightSubTree = dp.propagate(subTree.rightChild());

                        ImmutableSet<Variable> leftVariables = projectedVariables(subTree.leftChild(), leftGrandChild).immutableCopy();

                        Substitution<ImmutableTerm> naiveAscendingSubstitution = bindingLift.getAscendingSubstitution();
                        LiftableRightSubtree rightLiftableSubtree = getLiftableRightSubtree(Optional.empty(), rightSubTree, naiveAscendingSubstitution, leftVariables);

                        Substitution<ImmutableTerm> ascendingSubstitution =
                                substitutionFactory.union(
                                        naiveAscendingSubstitution.restrictDomainTo(leftVariables),
                                        rightLiftableSubtree.getLiftableSubstitution());

                        return Optional.of(state.lift(
                                createConstructionNode(subTree, ascendingSubstitution),
                                new LeftJoinSubTree(bindingLift.getCondition(), leftGrandChild, rightLiftableSubtree.getRightTree())));
                    }
                    catch (DownPropagation.InconsistentDownPropagationException e) {
                        // Replaces the LJ by the left child and stops recursion!
                        return Optional.of(state.lift(
                                createConstructionNode(subTree, constructionNode.getSubstitution()),
                                new LeftJoinSubTree(Optional.empty(), leftGrandChild, createEmptyRightChild(subTree))));
                    }
                }

                @Override
                public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree liftedLeftChild, DistinctNode distinctNode, IQTree leftGrandChild) {
                    // When the left is distinct, isDistinct() behaves like for inner joins
                    if (subTree.rightChild().isDistinct()
                            || iqTreeTools.createInnerJoinTree(subTree.ljCondition(), subTree.children()).isDistinct()) {
                        return Optional.of(state.lift(
                                distinctNode,
                                subTree.replaceChildren(leftGrandChild, subTree.rightChild().removeDistincts())));
                    }
                    return done();
                }

                @Override
                public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree liftedLeftChild, FilterNode filterNode, IQTree leftGrandChild) {
                    return Optional.of(state.lift(filterNode, subTree.replaceLeft(leftGrandChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree liftedLeftChild, InnerJoinNode joinNode, ImmutableList<IQTree> leftGrandChildren) {
                    Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                    if (joinCondition.isPresent()) {
                        // lifts the filter from the join, but stops recursion on the next iteration
                        return Optional.of(state.lift(
                                iqFactory.createFilterNode(joinCondition.get()),
                                subTree.replaceLeft(normalizeSubTreeRecursively(
                                        iqTreeTools.createInnerJoinTree(leftGrandChildren)))));
                    }
                    return done();
                }
            });
        }

        State<UnaryOperatorNode, LeftJoinSubTree> liftRightChild(State<UnaryOperatorNode, LeftJoinSubTree> s0) {
            var state = s0.replace(t -> t.replaceRight(normalizeSubTreeRecursively(t.rightChild())));
            return state.getSubTree().rightChild().acceptVisitor(new LiftRightChildStep(state))
                    .orElse(state);
        }

        private class LiftRightChildStep extends DefaultIQTreeOptionalVisitingTransformer<State<UnaryOperatorNode, LeftJoinSubTree>> {

            private final State<UnaryOperatorNode, LeftJoinSubTree> state;
            private final LeftJoinSubTree subTree;

            LiftRightChildStep(State<UnaryOperatorNode, LeftJoinSubTree> state) {
                this.state = state;
                this.subTree = state.getSubTree();
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode constructionNode, IQTree rightGrandChild) {
                Substitution<ImmutableTerm> rightSubstitution = constructionNode.getSubstitution();
                if (rightGrandChild instanceof TrueNode) {
                    Substitution<ImmutableTerm> liftableSubstitution = subTree.ljCondition()
                            .map(c -> rightSubstitution.<ImmutableTerm>transform(t -> termFactory.getIfElseNull(c, t)))
                            .orElse(rightSubstitution);

                    return Optional.of(state.lift(
                            createConstructionNode(subTree, liftableSubstitution),
                            subTree.replaceRight(rightGrandChild)));
                }

                if (rightSubstitution.isEmpty()) {
                    return Optional.of(state.lift(
                            createConstructionNode(subTree, rightSubstitution),
                            subTree.replaceRight(rightGrandChild)));
                }

                Optional<Variable> provenanceVariable = rightSubstitution
                        .getPreImage(t -> t.equals(specialProvenanceConstant))
                        .stream()
                        .findFirst();

                Substitution<ImmutableTerm> selectedSubstitution = provenanceVariable
                        .map(pv -> rightSubstitution.removeFromDomain(ImmutableSet.of(pv)))
                        .orElse(rightSubstitution);

                /*
                 * substitution with only a provenance entry -> see if something can be lifted from the grand child
                 */
                if (selectedSubstitution.isEmpty())
                    return liftRightGrandChildWithProvenance(state,
                            provenanceVariable
                                    .orElseThrow(() -> new MinorOntopInternalBugException("An entry was expected")),
                            constructionNode.getChildVariables(),
                            rightGrandChild);

                ImmutableSet<Variable> leftVariables = subTree.leftChild().getVariables();
                Optional<ImmutableExpression> notOptimizedLJCondition = termFactory.getConjunction(
                        subTree.ljCondition().map(selectedSubstitution::apply),
                        selectedSubstitution.builder()
                                .restrictDomainTo(leftVariables)
                                .toStream(termFactory::getStrictEquality));

                LiftableRightSubtree liftableRightSubtree = getLiftableRightSubtree(provenanceVariable, rightGrandChild, selectedSubstitution, leftVariables);

                // Tree where a fresh non-nullable variable may have been introduced for the provenance
                return Optional.of(state.lift(
                        createConstructionNode(subTree, liftableRightSubtree.getLiftableSubstitution()),
                        subTree.replaceRight(notOptimizedLJCondition, liftableRightSubtree.getRightTree())));
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandChild) {
                if (subTree.leftChild().isDistinct())
                    return Optional.of(state.lift(
                            distinctNode,
                            subTree.replaceChildren(subTree.leftChild().removeDistincts(), rightGrandChild)));

                return done();
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree rightGrandChild) {
                ImmutableExpression newLJCondition = iqTreeTools.getConjunction(subTree.ljCondition(), filterNode.getFilterCondition());
                return Optional.of(state.replace(
                        subTree.replaceRight(Optional.of(newLJCondition), rightGrandChild)));
            }

            @Override
            public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> grandChildren) {
                Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                // lifts the filter from the join, but stops recursion on the next iteration
                if (joinCondition.isPresent()) {
                    ImmutableExpression newLJCondition = iqTreeTools.getConjunction(subTree.ljCondition(), joinCondition.get());
                    return Optional.of(state.replace(
                            subTree.replaceRight(Optional.of(newLJCondition), iqTreeTools.createInnerJoinTree(grandChildren))));
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

                return rightGrandChild.acceptVisitor(new DefaultIQTreeOptionalVisitingTransformer<>() {
                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandGrandChild) {
                        if (subTree.leftChild().isDistinct()) {
                            IQTree newRightChild = rightProvenanceNormalizer.createProvenanceInConstructionNode(provenanceVariable, rightGrandGrandChild, rightGrandChild.getVariables());
                            return Optional.of(state.lift(
                                            optionalProjectingAwayParent,
                                            subTree.replaceRight(newRightChild)))
                                    .map(s -> s.lift(
                                            distinctNode, s.getSubTree().replaceLeft(s.getSubTree().leftChild().removeDistincts())));
                        }
                        return Optional.empty();
                    }

                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree rightGrandGrandChild) {
                        ImmutableExpression filterCondition = filterNode.getFilterCondition();
                        IQTree newRightChild = rightProvenanceNormalizer.createProvenanceInConstructionNode(provenanceVariable, rightGrandGrandChild, Sets.union(rightChildRequiredVariables, filterCondition.getVariables()));
                        ImmutableExpression newLJCondition = iqTreeTools.getConjunction(subTree.ljCondition(), filterCondition);
                        return Optional.of(state.lift(
                                optionalProjectingAwayParent,
                                subTree.replaceRight(Optional.of(newLJCondition), newRightChild)));
                    }

                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> grandGrandChildren) {
                        Optional<ImmutableExpression> joinCondition = joinNode.getOptionalFilterCondition();
                        if (joinCondition.isPresent()) {
                            NaryIQTree newRightGrandChild = iqTreeTools.createInnerJoinTree(grandGrandChildren);
                            IQTree newRightChild = rightProvenanceNormalizer.createProvenanceInConstructionNode(provenanceVariable, newRightGrandChild, Sets.union(rightChildRequiredVariables, joinCondition.get().getVariables()));
                            ImmutableExpression newLJCondition = iqTreeTools.getConjunction(subTree.ljCondition(), joinCondition.get());
                            return Optional.of(state.lift(
                                    optionalProjectingAwayParent,
                                    subTree.replaceRight(Optional.of(newLJCondition), newRightChild)));
                        }
                        return Optional.empty();
                    }
                });
            }
        }

        private State<UnaryOperatorNode, LeftJoinSubTree> optimizeLeftJoinCondition(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (subTree.ljCondition().isEmpty())
                return state;

            try {
                ConditionSimplifier.ExpressionAndSubstitution simplification = conditionSimplifier.simplifyCondition(
                        subTree.ljCondition(),
                        subTree.leftChild().getVariables(),
                        ImmutableList.of(subTree.rightChild()),
                        variableNullabilityTools.getChildrenVariableNullability(subTree.children()));

                DownPropagation dpR = iqTreeTools.getDownPropagation(simplification, subTree.rightChild().getVariables(), variableGenerator);
                if (dpR.getDescendingSubstitution().isEmpty()) {
                    return state.replace(t -> t.replaceRight(simplification.getOptionalExpression(), t.rightChild()));
                }

                IQTree updatedRightChild = dpR.propagate(subTree.rightChild());
                var liftableRightSubtree = getLiftableRightSubtree(
                        Optional.empty(), updatedRightChild, dpR.getDescendingSubstitution(), subTree.leftChild().getVariables());

                return state.lift(
                        createConstructionNode(subTree, liftableRightSubtree.getLiftableSubstitution()),
                        subTree.replaceRight(simplification.getOptionalExpression(), liftableRightSubtree.getRightTree()));
            }
            catch (DownPropagation.InconsistentDownPropagationException e) {
                return state.replace(t -> t.replaceRight(Optional.empty(), createEmptyRightChild(t)));
            }
        }

        protected IQTree asIQTree(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (subTree.isEmpty())
                return createEmptyNode();

            IQTree ljLevelTree;
            if (subTree.isRightChildEmpty()) {
                var paddingConstructionNode = createConstructionNode(subTree,
                        subTree.rightSpecificVariables().stream()
                                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));

                ljLevelTree = iqFactory.createUnaryIQTree(paddingConstructionNode, subTree.leftChild(), getNormalizedTreeCache(true));
            }
            else if (subTree.rightChild() instanceof TrueNode) {
                ljLevelTree = subTree.leftChild();
            }
            else {
                ljLevelTree = iqFactory.createBinaryNonCommutativeIQTree(
                        iqFactory.createLeftJoinNode(subTree.ljCondition()), subTree.leftChild(), subTree.rightChild(), getNormalizedTreeCache(true));
            }

            // Normalizes the ancestors
            return normalizeSubTreeRecursively(
                    iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                            .append(state.getAncestors())
                            .build(ljLevelTree));
        }


        public State<UnaryOperatorNode, LeftJoinSubTree> propagateDownLJCondition(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            DownPropagation dc = iqTreeTools.createDownPropagation(subTree.ljCondition(), subTree.projectedVariables(), variableGenerator);
            IQTree newRightChild = dc.propagateWithRestrictedScope(subTree.rightChild());
            return state.replace(subTree.replaceRight(newRightChild));
        }

        /**
         * If the right child does not contribute new variables and does not change the cardinality,
         * we can drop it
         */
        public State<UnaryOperatorNode, LeftJoinSubTree> checkRightChildContribution(State<UnaryOperatorNode, LeftJoinSubTree> state) {
            LeftJoinSubTree subTree = state.getSubTree();
            if (subTree.rightSpecificVariables().isEmpty()
                    && !subTree.rightChild().inferUniqueConstraints().isEmpty()) {
                return state.replace(
                        subTree.replaceRight(Optional.empty(), iqFactory.createTrueNode()));
            }
            return state;
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private LiftableRightSubtree getLiftableRightSubtree(Optional<Variable> optionalProvenanceVariable,
                                                             IQTree rightTree,
                                                             Substitution<? extends ImmutableTerm> selectedSubstitution,
                                                             ImmutableSet<Variable> leftVariables) {

            var liftableSubstitutionBuilder = new LiftableSubstitutionBuilder(leftVariables, selectedSubstitution);
            if (optionalProvenanceVariable.isPresent()) {
                var provenanceVariable = optionalProvenanceVariable.get();
                var tree = rightProvenanceNormalizer.createProvenanceInConstructionNode(provenanceVariable, rightTree);
                return new LiftableRightSubtree(tree, liftableSubstitutionBuilder.build(Optional.of(provenanceVariable)));
            }

            if (!liftableSubstitutionBuilder.requiresProvenanceVariable()) {
                return new LiftableRightSubtree(rightTree, liftableSubstitutionBuilder.build(Optional.empty()));
            }

            var rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(rightTree, leftVariables, variableGenerator, rightTree.getVariableNullability());
            return new LiftableRightSubtree(rightProvenance.getTree(), liftableSubstitutionBuilder.build(Optional.of(rightProvenance.getProvenanceVariable())));
        }

        private class LiftableSubstitutionBuilder {
            private final ImmutableSet<Variable> leftVariables;
            private final Substitution<? extends ImmutableTerm> rightSpecificSubstitution;

            LiftableSubstitutionBuilder(ImmutableSet<Variable> leftVariables, Substitution<? extends ImmutableTerm> selectedSubstitution) {
                this.leftVariables = leftVariables;
                this.rightSpecificSubstitution = selectedSubstitution.removeFromDomain(leftVariables);
            }

            boolean requiresProvenanceVariable() {
                return rightSpecificSubstitution.rangeAnyMatch(term ->
                                !isNullWhenRightIsRejected(term)
                                && getProvenanceVariableProposal(term).isEmpty());
            }

            /**
             * Return true when the term is guaranteed to be NULL when the right is rejected
             */
            private boolean isNullWhenRightIsRejected(ImmutableTerm immutableTerm) {
                Substitution<ImmutableTerm> nullSubstitution =
                        Sets.difference(immutableTerm.getVariableStream().collect(ImmutableCollectors.toSet()), leftVariables).stream()
                                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()));

                return nullSubstitution.applyToTerm(immutableTerm)
                        .simplify()
                        .isNull();
            }

            private Optional<Variable> getProvenanceVariableProposal(ImmutableTerm term) {
                return Optional.of(term)
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t)
                        .flatMap(f -> f.proposeProvenanceVariables()
                                .filter(v -> !leftVariables.contains(v))
                                .findAny());
            }

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Substitution<ImmutableTerm> build(Optional<Variable> optionalProvenanceVariable) {
                return rightSpecificSubstitution
                        .transform(t -> transformRightSubstitutionValue(t, optionalProvenanceVariable));
            }

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            private ImmutableTerm transformRightSubstitutionValue(ImmutableTerm term, Optional<Variable> optionalProvenanceVariable) {
                if (isNullWhenRightIsRejected(term))
                    return term;

                Variable provenanceVariable = getProvenanceVariableProposal(term)
                        .or(() -> optionalProvenanceVariable)
                        .orElseThrow(() -> new MinorOntopInternalBugException("A default provenance variable was needed"));

                return termFactory.getIfElseNull(termFactory.getDBIsNotNull(provenanceVariable), term);
            }
        }
    }

    private static class LiftableRightSubtree {
        private final IQTree rightTree;
        private final Substitution<ImmutableTerm> liftableSubstitution;

        private LiftableRightSubtree(IQTree rightTree, Substitution<ImmutableTerm>  liftableSubstitution) {
            this.rightTree = rightTree;
            this.liftableSubstitution = liftableSubstitution;
        }

        IQTree getRightTree() {
            return rightTree;
        }

        Substitution<ImmutableTerm> getLiftableSubstitution() {
            return liftableSubstitution;
        }
    }
}
