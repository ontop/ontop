package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
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

            return subTree.leftChild().acceptVisitor(new IQStateOptionalTransformer<>() {

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

                        IQTree rightSubTree = subTree.rightChild().applyDescendingSubstitution(bindingLift.getDescendingSubstitution(), bindingLift.getCondition(), variableGenerator);

                        ImmutableSet<Variable> leftVariables = projectedVariables(subTree.leftChild(), leftGrandChild).immutableCopy();

                        Substitution<ImmutableTerm> naiveAscendingSubstitution = bindingLift.getAscendingSubstitution();
                        OptionalRightProvenance rightProvenance = new OptionalRightProvenance(rightSubTree, naiveAscendingSubstitution, leftVariables);

                        Optional<Variable> defaultProvenanceVariable = rightProvenance.getProvenanceVariable();
                        Substitution<ImmutableTerm> ascendingSubstitution =
                                naiveAscendingSubstitution.builder()
                                        .transformOrRetain(v -> !leftVariables.contains(v) ? v : null,
                                                (t, v) -> transformRightSubstitutionValue(t, leftVariables, defaultProvenanceVariable))
                                        .build();

                        return Optional.of(state.lift(
                                createConstructionNode(subTree, ascendingSubstitution),
                                new LeftJoinSubTree(bindingLift.getCondition(), leftGrandChild, rightProvenance.getRightTree())));
                    }
                    catch (UnsatisfiableConditionException e) {
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

        private class LiftRightChildStep extends IQStateOptionalTransformer<State<UnaryOperatorNode, LeftJoinSubTree>> {

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

                ImmutableSet<Variable> leftVariables = subTree.leftChild().getVariables();
                Optional<ImmutableExpression> notOptimizedLJCondition = termFactory.getConjunction(
                        subTree.ljCondition().map(selectedSubstitution::apply),
                        selectedSubstitution.builder()
                                .restrictDomainTo(leftVariables)
                                .toStream(termFactory::getStrictEquality));

                // TODO: only create a right provenance when really needed
                OptionalRightProvenance rightProvenance = provenanceVariable
                        .map(v -> new OptionalRightProvenance(v, rightGrandChild, selectedSubstitution, leftVariables, rightChildRequiredVariables))
                        .orElseGet(() -> new OptionalRightProvenance(rightGrandChild, selectedSubstitution, leftVariables, rightChildRequiredVariables));

                // Tree where a fresh non-nullable variable may have been introduced for the provenance
                return Optional.of(state.lift(
                        createConstructionNode(subTree, rightProvenance.computeLiftableSubstitution()),
                        subTree.replaceRight(notOptimizedLJCondition, rightProvenance.getRightTree())));
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

                return rightGrandChild.acceptVisitor(new IQStateOptionalTransformer<>() {
                    @Override
                    public Optional<State<UnaryOperatorNode, LeftJoinSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode distinctNode, IQTree rightGrandGrandChild) {
                        if (subTree.leftChild().isDistinct()) {
                            IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                    rightGrandGrandChild, rightGrandChild.getVariables());

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

                        IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                rightGrandGrandChild, Sets.union(rightChildRequiredVariables, filterCondition.getVariables()));

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

                            IQTree newRightChild = createSubTreeWithProvenance(provenanceVariable,
                                    newRightGrandChild, Sets.union(rightChildRequiredVariables, joinCondition.get().getVariables()));

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
                ConditionSimplifier.ExpressionAndSubstitution simplificationResults = conditionSimplifier.simplifyCondition(
                        subTree.ljCondition(), subTree.leftChild().getVariables(), ImmutableList.of(subTree.rightChild()),
                        variableNullabilityTools.getChildrenVariableNullability(subTree.children()));

                Substitution<? extends VariableOrGroundTerm> downSubstitution =
                        simplificationResults.getSubstitution()
                                .restrictDomainTo(subTree.rightChild().getVariables());

                var optionalCondition = simplificationResults.getOptionalExpression();
                if (downSubstitution.isEmpty()) {
                    return state.replace(t -> t.replaceRight(optionalCondition, t.rightChild()));
                }

                IQTree updatedRightChild = subTree.rightChild().applyDescendingSubstitution(
                        downSubstitution, optionalCondition, variableGenerator);

                var rightProvenance = new OptionalRightProvenance(
                        updatedRightChild, downSubstitution, subTree.leftChild().getVariables());

                return state.lift(
                        createConstructionNode(subTree, rightProvenance.computeLiftableSubstitution()),
                        subTree.replaceRight(optionalCondition, rightProvenance.getRightTree()));
            }
            catch (UnsatisfiableConditionException e) {
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
            DownPropagation dc = DownPropagation.of(subTree.ljCondition(), subTree.rightChild().getVariables(), variableGenerator, termFactory);
            IQTree newRightChild = dc.propagate(subTree.rightChild());
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
