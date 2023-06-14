package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.LeftJoinNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer.RightProvenance;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class LeftJoinNormalizerImpl implements LeftJoinNormalizer {

    private static final int MAX_ITERATIONS = 10000;

    private final Constant specialProvenanceConstant;

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ConditionSimplifier conditionSimplifier;
    private final JoinLikeChildBindingLifter bindingLifter;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;

    private final IQTreeTools iqTreeTools;


    @Inject
    private LeftJoinNormalizerImpl(SubstitutionFactory substitutionFactory, TermFactory termFactory,
                                   IntermediateQueryFactory iqFactory, ConditionSimplifier conditionSimplifier,
                                   JoinLikeChildBindingLifter bindingLifter,
                                   JoinOrFilterVariableNullabilityTools variableNullabilityTools, RightProvenanceNormalizer rightProvenanceNormalizer,
                                   IQTreeTools iqTreeTools) {
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.conditionSimplifier = conditionSimplifier;
        this.bindingLifter = bindingLifter;
        this.variableNullabilityTools = variableNullabilityTools;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.iqTreeTools = iqTreeTools;

        specialProvenanceConstant = termFactory.getProvenanceSpecialConstant();
    }


    @Override
    public IQTree normalizeForOptimization(LeftJoinNode ljNode, IQTree initialLeftChild, IQTree initialRightChild,
                                           VariableGenerator variableGenerator,
                                           IQTreeCache treeCache) {

        ImmutableSet<Variable> projectedVariables = Stream.of(initialLeftChild, initialRightChild)
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        // Non-final
        LJNormalizationState state = new LJNormalizationState(projectedVariables, initialLeftChild, initialRightChild,
                ljNode.getOptionalFilterCondition(), variableGenerator);

        // The left child cannot be made empty because of the LJ. Therefore this step is enough to detect emptiness.
        state = state.liftLeftChild();
        if (state.isEmpty())
            return state.createNormalizedTree(treeCache);

        // Particularly needed when the LJ condition has never been propagated down
        // and no substitution on both side will give an opportunity.
        // TODO: see if it deserves to be in the loop.
        state = state.propagateDownLJCondition();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            LJNormalizationState newState = state
                    .checkRightChildContribution()
                    .optimizeLeftJoinCondition()
                    .liftRightChild()
                    // A DISTINCT on the left might have been waiting because of a not-yet distinct right child
                    .liftLeftChild();

            if (state.equals(newState))
                return state.createNormalizedTree(treeCache);

            state = newState;
        }
        throw new MinorOntopInternalBugException("LJ.normalizeForOptimization() did not converge after " + MAX_ITERATIONS);
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class LJNormalizationState {

        private final IQTree leftChild;
        private final IQTree rightChild;
        private final VariableGenerator variableGenerator;
        private final Optional<ImmutableExpression> ljCondition;
        // Parent first
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final ImmutableSet<Variable> projectedVariables;

        private LJNormalizationState(ImmutableSet<Variable> projectedVariables, IQTree leftChild, IQTree rightChild,
                                     Optional<ImmutableExpression> ljCondition,
                                     ImmutableList<UnaryOperatorNode> ancestors,
                                     VariableGenerator variableGenerator) {
            this.projectedVariables = projectedVariables;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.ljCondition = ljCondition;
            this.ancestors = ancestors;
            this.variableGenerator = variableGenerator;
        }

        protected LJNormalizationState(ImmutableSet<Variable> projectedVariables, IQTree initialLeftChild,
                                       IQTree initialRightChild, Optional<ImmutableExpression> ljCondition,
                                       VariableGenerator variableGenerator) {
            this(projectedVariables, initialLeftChild, initialRightChild, ljCondition, ImmutableList.of(), variableGenerator);
        }

        private LJNormalizationState updateConditionAndRightChild(Optional<ImmutableExpression> newLJCondition,
                                                                  IQTree newRightChild) {
            if (ljCondition.equals(newLJCondition) && rightChild.equals(newRightChild))
                return this;

            return new LJNormalizationState(projectedVariables, leftChild, newRightChild, newLJCondition,
                    ancestors, variableGenerator);
        }

        private LJNormalizationState updateParentConditionRightChild(
                UnaryOperatorNode newParent, Optional<ImmutableExpression> newLJCondition, IQTree newRightChild) {
            return updateParentConditionChildren(newParent, newLJCondition, leftChild, newRightChild);
        }

        private LJNormalizationState updateParentConditionChildren(UnaryOperatorNode newParent,
                                                                   Optional<ImmutableExpression> newLJCondition,
                                                                   IQTree newLeftChild, IQTree newRightChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new LJNormalizationState(projectedVariables, newLeftChild, newRightChild, newLJCondition,
                    newAncestors, variableGenerator);
        }

        private LJNormalizationState updateAncestorsConditionChildren(ImmutableList<UnaryOperatorNode> additionalAncestors,
                                                                   Optional<ImmutableExpression> newLJCondition,
                                                                   IQTree newLeftChild, IQTree newRightChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .addAll(additionalAncestors)
                    .addAll(ancestors)
                    .build();

            return new LJNormalizationState(projectedVariables, newLeftChild, newRightChild, newLJCondition,
                    newAncestors, variableGenerator);
        }

        private LJNormalizationState updateLeftChild(IQTree newLeftChild) {
            return new LJNormalizationState(projectedVariables, newLeftChild, rightChild, ljCondition,
                    ancestors, variableGenerator);
        }

        public LJNormalizationState liftLeftChild() {
            IQTree liftedLeftChild = leftChild.normalizeForOptimization(variableGenerator);
            QueryNode leftRootNode = liftedLeftChild.getRootNode();

            if (leftRootNode instanceof ConstructionNode)
                return liftLeftConstruction((UnaryIQTree) liftedLeftChild);
            else if (leftRootNode instanceof DistinctNode)
                return liftLeftDistinct((UnaryIQTree) liftedLeftChild);
            else if (leftRootNode instanceof FilterNode)
                return liftLeftFilterNode((UnaryIQTree) liftedLeftChild);
            else if (leftRootNode instanceof CommutativeJoinNode)
                return liftLeftCommutativeJoin(liftedLeftChild);
            else if (liftedLeftChild.isDeclaredAsEmpty())
                // Stops the liftLeftChild() recursion
                return new LJNormalizationState(projectedVariables, liftedLeftChild,
                        iqFactory.createEmptyNode(rightChild.getVariables()), Optional.empty(),
                        ancestors, variableGenerator);
            else
                // Stops the liftLeftChild() recursion
                return updateLeftChild(liftedLeftChild);
        }

        private LJNormalizationState liftLeftConstruction(UnaryIQTree liftedLeftChild) {
            ConstructionNode leftConstructionNode = (ConstructionNode) liftedLeftChild.getRootNode();
            IQTree leftGrandChild = liftedLeftChild.getChild();

            try {
                ImmutableList<IQTree> children = ImmutableList.of(liftedLeftChild, rightChild);
                VariableNullability childVariableNullability = variableNullabilityTools.getChildrenVariableNullability(
                        ImmutableList.of(leftGrandChild, rightChild));

                return bindingLifter.liftRegularChildBinding(leftConstructionNode, 0, leftGrandChild,
                        children,
                        leftGrandChild.getVariables(), ljCondition, variableGenerator,
                        childVariableNullability, this::applyLeftChildBindingLift)
                        // Recursive (for optimization purposes)
                        .liftLeftChild();
            }
            /*
             * Replaces the LJ by the left child
             */
            catch (UnsatisfiableConditionException e) {
                EmptyNode newRightChild = iqFactory.createEmptyNode(rightChild.getVariables());

                ConstructionNode newParentConstructionNode = iqFactory.createConstructionNode(
                        iqTreeTools.getChildrenVariables(liftedLeftChild, rightChild),
                        leftConstructionNode.getSubstitution());

                // Stops the liftLeftChild() recursion
                return updateParentConditionChildren(newParentConstructionNode, Optional.empty(), leftGrandChild, newRightChild);
            }
        }

        private LJNormalizationState liftLeftDistinct(UnaryIQTree liftedLeftChild) {
            DistinctNode distinctNode = (DistinctNode) liftedLeftChild.getRootNode();
            if (isLJDistinctWhileLeftIsDistinct(liftedLeftChild)) {
                IQTree newRightChild = rightChild.removeDistincts();
                IQTree newLeftChild = liftedLeftChild.getChild();
                return updateParentConditionChildren(distinctNode, ljCondition, newLeftChild, newRightChild)
                        // Recursive (for optimization purposes)
                        .liftLeftChild();
            }
            else
                // Stops the liftLeftChild() recursion
                return updateLeftChild(liftedLeftChild);
        }

        /**
         * When the left is distinct, isDistinct() behaves like for inner joins
         */
        private boolean isLJDistinctWhileLeftIsDistinct(IQTree distinctLeftChild) {
            if (rightChild.isDistinct())
                return true;

            IQTree innerJoinTree = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(ljCondition),
                    ImmutableList.of(distinctLeftChild, rightChild));

            return innerJoinTree.isDistinct();
        }

        private Optional<LJNormalizationState> tryToLiftRightDistinct(UnaryIQTree liftedRightChild) {
            DistinctNode distinctNode = (DistinctNode) liftedRightChild.getRootNode();
            if (leftChild.isDistinct()) {
                IQTree newLeftChild = leftChild.removeDistincts();
                IQTree newRightChild = liftedRightChild.getChild();
                return Optional.of(updateParentConditionChildren(distinctNode, ljCondition, newLeftChild, newRightChild));
            }
            else
                return Optional.empty();
        }

        private LJNormalizationState liftLeftFilterNode(UnaryIQTree liftedLeftChild) {
            FilterNode filterNode = (FilterNode) liftedLeftChild.getRootNode();
            return updateParentConditionChildren(filterNode, ljCondition, liftedLeftChild.getChild(), rightChild);
        }

        private LJNormalizationState liftLeftCommutativeJoin(IQTree liftedLeftChild) {
            CommutativeJoinNode joinNode = (CommutativeJoinNode) liftedLeftChild.getRootNode();

            Optional<ImmutableExpression> filterCondition = joinNode.getOptionalFilterCondition();
            if (filterCondition.isPresent()) {

                FilterNode newParent = iqFactory.createFilterNode(filterCondition.get());

                NaryIQTree newLeftChild = iqFactory.createNaryIQTree(
                        joinNode.changeOptionalFilterCondition(Optional.empty()),
                        liftedLeftChild.getChildren());

                return updateParentConditionChildren(newParent, ljCondition, newLeftChild, rightChild);
            }
            else
                return updateLeftChild(liftedLeftChild);
        }


        private LJNormalizationState liftRightFilter(UnaryIQTree liftedRightChild) {
            FilterNode filterNode = (FilterNode) liftedRightChild.getRootNode();

            ImmutableExpression newLJCondition = ljCondition
                    .map(c -> termFactory.getConjunction(c, filterNode.getFilterCondition()))
                    .orElseGet(filterNode::getFilterCondition);

            return updateConditionAndRightChild(Optional.of(newLJCondition), liftedRightChild.getChild());
        }

        private Optional<LJNormalizationState> tryToLiftRightCommutativeJoin(IQTree liftedRightChild) {
            CommutativeJoinNode joinNode = (CommutativeJoinNode) liftedRightChild.getRootNode();

            Optional<ImmutableExpression> filterCondition = joinNode.getOptionalFilterCondition();
            if (filterCondition.isPresent()) {
                ImmutableExpression condition = filterCondition.get();
                ImmutableExpression newLJCondition = ljCondition
                        .map(c -> termFactory.getConjunction(c, condition))
                        .orElse(condition);

                NaryIQTree newRightChild = iqFactory.createNaryIQTree(
                        joinNode.changeOptionalFilterCondition(Optional.empty()),
                        liftedRightChild.getChildren());

                return Optional.of(updateConditionAndRightChild(Optional.of(newLJCondition), newRightChild));
            }
            else
                return Optional.empty();
        }

        private LJNormalizationState applyLeftChildBindingLift(
                ImmutableList<IQTree> children, IQTree leftGrandChild, int leftChildPosition,
                Optional<ImmutableExpression> ljCondition, Substitution<ImmutableTerm> naiveAscendingSubstitution,
                Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {

            if (children.size() != 2)
                throw new MinorOntopInternalBugException("Two children were expected, not " + children);

            IQTree initialRightChild = children.get(1);
            IQTree rightSubTree = initialRightChild.applyDescendingSubstitution(descendingSubstitution, ljCondition, variableGenerator);

            ImmutableSet<Variable> leftVariables = iqTreeTools.getChildrenVariables(children.get(0), leftGrandChild);

            // Creates a right provenance if needed for lifting the substitution
            Optional<RightProvenance> rightProvenance = createProvenanceElements(rightSubTree,
                    naiveAscendingSubstitution, leftVariables, rightSubTree.getVariables(), variableGenerator);

            IQTree newRightChild = rightProvenance.map(RightProvenance::getRightTree)
                    .orElse(rightSubTree);

            Optional<Variable> defaultProvenanceVariable = rightProvenance.map(RightProvenance::getProvenanceVariable);

            Substitution<ImmutableTerm> ascendingSubstitution =
                    naiveAscendingSubstitution.builder()
                            .transformOrRetain(v -> !leftVariables.contains(v) ? v : null,
                                    (t, v) -> transformRightSubstitutionValue(t, leftVariables, defaultProvenanceVariable))
                            .build();

            ConstructionNode parentConstructionNode = iqFactory.createConstructionNode(
                    iqTreeTools.getChildrenVariables(children), ascendingSubstitution);

            return updateParentConditionChildren(parentConstructionNode, ljCondition, leftGrandChild, newRightChild);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LJNormalizationState))
                return false;

            LJNormalizationState other = (LJNormalizationState) o;
            return leftChild.equals(other.leftChild)
                    && rightChild.equals(other.rightChild)
                    && ljCondition.equals(other.ljCondition)
                    && ancestors.equals(other.ancestors);
        }

        private LJNormalizationState optimizeLeftJoinCondition() {
            if (!ljCondition.isPresent())
                return this;

            ImmutableSet<Variable> leftVariables = leftChild.getVariables();
            ImmutableSet<Variable> rightVariables = rightChild.getVariables();

            try {
                ConditionSimplifier.ExpressionAndSubstitution simplificationResults = conditionSimplifier.simplifyCondition(
                        ljCondition, leftVariables, ImmutableList.of(rightChild),
                        variableNullabilityTools.getChildrenVariableNullability(ImmutableList.of(leftChild, rightChild)));

                Substitution<? extends VariableOrGroundTerm> downSubstitution =
                                simplificationResults.getSubstitution().restrictDomainTo(rightVariables);

                if (downSubstitution.isEmpty())
                    return updateConditionAndRightChild(simplificationResults.getOptionalExpression(), rightChild);

                IQTree updatedRightChild = rightChild.applyDescendingSubstitution(downSubstitution,
                        simplificationResults.getOptionalExpression(), variableGenerator);

                Optional<RightProvenance> rightProvenance = createProvenanceElements(updatedRightChild, downSubstitution,
                        leftVariables, updatedRightChild.getVariables(), variableGenerator);

                IQTree newRightChild = rightProvenance.map(RightProvenance::getRightTree)
                        .orElse(updatedRightChild);

                Substitution<ImmutableTerm> newAscendingSubstitution = computeLiftableSubstitution(
                        downSubstitution, rightProvenance.map(RightProvenance::getProvenanceVariable), leftVariables);

                ConstructionNode newParentConstructionNode = iqFactory.createConstructionNode(
                        iqTreeTools.getChildrenVariables(leftChild, rightChild),
                        newAscendingSubstitution);

                return updateParentConditionChildren(newParentConstructionNode,
                        simplificationResults.getOptionalExpression(),  leftChild, newRightChild);

            } catch (UnsatisfiableConditionException e) {
                return updateConditionAndRightChild(Optional.empty(), iqFactory.createEmptyNode(rightVariables));
            }
        }

        private LJNormalizationState liftRightChild() {
            IQTree liftedRightChild = rightChild.normalizeForOptimization(variableGenerator);

            return tryToLiftRightChild(liftedRightChild)
                    // If not nothing can be lifted above, makes at least sure the right child is normalized
                    .orElseGet(() -> updateConditionAndRightChild(
                            ljCondition.filter(c -> !liftedRightChild.isDeclaredAsEmpty()),
                            liftedRightChild));
        }

        /**
         * Tries to lift something from the right above the left join
         */
        private Optional<LJNormalizationState> tryToLiftRightChild(IQTree liftedRightChild) {
            QueryNode rightRootNode = liftedRightChild.getRootNode();
            if (rightRootNode instanceof ConstructionNode) {
                ConstructionNode rightConstructionNode = (ConstructionNode) liftedRightChild.getRootNode();
                IQTree rightGrandChild = ((UnaryIQTree) liftedRightChild).getChild();

                Substitution<ImmutableTerm> rightSubstitution = rightConstructionNode.getSubstitution();
                return tryToLiftRightConstruction(rightConstructionNode.getChildVariables(), rightGrandChild,
                        rightSubstitution);
            }
            else if (rightRootNode instanceof DistinctNode) {
                return tryToLiftRightDistinct((UnaryIQTree) liftedRightChild);
            }
            else if (rightRootNode instanceof FilterNode) {
                return Optional.of(liftRightFilter((UnaryIQTree)liftedRightChild));
            }
            else if (rightRootNode instanceof CommutativeJoinNode) {
                return tryToLiftRightCommutativeJoin(liftedRightChild);
            }
            else
                return Optional.empty();
        }

        private Optional<LJNormalizationState> tryToLiftRightConstruction(ImmutableSet<Variable> rightChildRequiredVariables, IQTree rightGrandChild,
                                                                          Substitution<ImmutableTerm> rightSubstitution) {
            if (rightGrandChild instanceof TrueNode)
                return liftRightConstructionWithTrueNode(rightSubstitution);

            ImmutableSet<Variable> leftVariables = leftChild.getVariables();

            // Empty substitution -> replace the construction node by its child
            if (rightSubstitution.isEmpty()) {
                ConstructionNode newParent = iqFactory.createConstructionNode(iqTreeTools.getChildrenVariables(leftChild, rightChild));
                return Optional.of(updateParentConditionRightChild(newParent, ljCondition, rightGrandChild));
            }

            Optional<Variable> provenanceVariable = rightSubstitution.getPreImage(t -> t.equals(specialProvenanceConstant))
                    .stream()
                    .findFirst();

            Substitution<ImmutableTerm> selectedSubstitution = provenanceVariable
                    .map(pv -> rightSubstitution.removeFromDomain(ImmutableSet.of(pv)))
                    .orElse(rightSubstitution);

            /*
             * substitution with only a provenance entry -> see if something can be lifted from the grand child
             */
            if (selectedSubstitution.isEmpty())
                return liftRightGrandChildWithProvenance(
                        provenanceVariable.orElseThrow(() -> new MinorOntopInternalBugException("An entry was expected")),
                        rightChildRequiredVariables,
                        rightGrandChild);

            Optional<ImmutableExpression> notOptimizedLJCondition = termFactory.getConjunction(
                    ljCondition.map(selectedSubstitution::apply),
                    selectedSubstitution.builder()
                            .restrictDomainTo(leftVariables)
                            .toStream(termFactory::getStrictEquality));

            // TODO: only create a right provenance when really needed
            Optional<RightProvenance> rightProvenance = provenanceVariable
                    .map(v -> createProvenanceElements(v, rightGrandChild, rightChildRequiredVariables))
                    .or(() -> createProvenanceElements(rightGrandChild, selectedSubstitution,
                            leftVariables, rightChildRequiredVariables, variableGenerator));

            // Tree where a fresh non-nullable variable may have been introduced for the provenance
            IQTree newRightChild = rightProvenance.map(RightProvenance::getRightTree)
                    .orElse(rightGrandChild);

            Substitution<ImmutableTerm> liftableSubstitution = computeLiftableSubstitution(
                    selectedSubstitution, rightProvenance.map(RightProvenance::getProvenanceVariable), leftVariables);

            ConstructionNode newParentNode = iqFactory.createConstructionNode(
                    iqTreeTools.getChildrenVariables(leftChild, rightChild),
                    liftableSubstitution);

            return Optional.of(updateParentConditionRightChild(newParentNode, notOptimizedLJCondition, newRightChild));
        }

        /**
         * Right child: a ConstructionNode followed by a TrueNode
         */
        private Optional<LJNormalizationState> liftRightConstructionWithTrueNode(Substitution<ImmutableTerm> rightSubstitution) {

            Substitution<ImmutableTerm> liftableSubstitution = ljCondition
                    .map(c -> rightSubstitution.<ImmutableTerm>transform(t -> termFactory.getIfElseNull(c, t)))
                    .orElse(rightSubstitution);

            ConstructionNode newParentNode = iqFactory.createConstructionNode(
                    iqTreeTools.getChildrenVariables(leftChild, rightChild),
                    liftableSubstitution);

            return Optional.of(updateParentConditionRightChild(newParentNode, ljCondition, iqFactory.createTrueNode()));
        }

        /**
         * TODO: find a better name
         *
         * When the right child is composed of a construction node with only a provenance entry
         *
         */
        private Optional<LJNormalizationState> liftRightGrandChildWithProvenance(Variable provenanceVariable,
                                                                       ImmutableSet<Variable> rightChildRequiredVariables,
                                                                       IQTree rightGrandChild) {
            QueryNode grandChildNode = rightGrandChild.getRootNode();

            // Parent construction node: in case some variables where projected out by the right construction node
            Optional<ConstructionNode> optionalProjectingAwayParent = Optional.of(rightChild.getVariables())
                    .filter(rvs -> !rightChildRequiredVariables.equals(rightGrandChild.getVariables()))
                    .map(rvs -> Sets.union(leftChild.getVariables(), rvs).immutableCopy())
                    .map(iqFactory::createConstructionNode);

            if (grandChildNode instanceof DistinctNode) {
                if (leftChild.isDistinct()) {
                    IQTree newLeftChild = leftChild.removeDistincts();
                    IQTree newRightChild = createNewRightChildWithProvenance(provenanceVariable,
                            ((UnaryIQTree)rightGrandChild).getChild(), rightGrandChild.getVariables());

                    ImmutableList<UnaryOperatorNode> additionalAncestors = optionalProjectingAwayParent
                            .map(p -> ImmutableList.of((DistinctNode) grandChildNode, p))
                            .orElseGet(() -> ImmutableList.of((DistinctNode) grandChildNode));

                    return Optional.of(updateAncestorsConditionChildren(additionalAncestors, ljCondition, newLeftChild, newRightChild));
                }
                else
                    return Optional.empty();
            }
            else if (grandChildNode instanceof FilterNode) {
                FilterNode filterNode = (FilterNode) grandChildNode;
                ImmutableExpression filterCondition = filterNode.getFilterCondition();

                ImmutableExpression newLJCondition = ljCondition
                        .map(c -> termFactory.getConjunction(c, filterCondition))
                        .orElse(filterCondition);

                ImmutableSet<Variable> childVariablesToProject = Sets.union(rightChildRequiredVariables, filterCondition.getVariables())
                        .immutableCopy();

                IQTree newRightChild = createNewRightChildWithProvenance(provenanceVariable,
                        ((UnaryIQTree)rightGrandChild).getChild(), childVariablesToProject);

                return Optional.of(optionalProjectingAwayParent
                        .map(p -> updateParentConditionRightChild(p, Optional.of(newLJCondition), newRightChild))
                        .orElseGet(() -> updateConditionAndRightChild(Optional.of(newLJCondition), newRightChild)));
            }
            else if (grandChildNode instanceof CommutativeJoinNode) {
                CommutativeJoinNode joinNode = (CommutativeJoinNode) grandChildNode;

                Optional<ImmutableExpression> filterCondition = joinNode.getOptionalFilterCondition();
                if (filterCondition.isPresent()) {
                    ImmutableExpression condition = filterCondition.get();
                    ImmutableExpression newLJCondition = ljCondition
                            .map(c -> termFactory.getConjunction(c, condition))
                            .orElse(condition);

                    NaryIQTree newRightGrandChild = iqFactory.createNaryIQTree(
                            joinNode.changeOptionalFilterCondition(Optional.empty()),
                            rightGrandChild.getChildren());

                    ImmutableSet<Variable> childVariablesToProject = Sets.union(rightChildRequiredVariables, condition.getVariables())
                            .immutableCopy();

                    IQTree newRightChild = createNewRightChildWithProvenance(provenanceVariable, newRightGrandChild,
                            childVariablesToProject);

                    return Optional.of(optionalProjectingAwayParent
                            .map(p -> updateParentConditionRightChild(p, Optional.of(newLJCondition), newRightChild))
                            .orElseGet(() -> updateConditionAndRightChild(Optional.of(newLJCondition), newRightChild)));
                }
                else
                    return Optional.empty();
            }
            else
                return Optional.empty();
        }

        private IQTree createNewRightChildWithProvenance(Variable provenanceVariable, IQTree child,
                                                         ImmutableSet<Variable> childVariablesToProject) {
            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                    Sets.union(ImmutableSet.of(provenanceVariable), childVariablesToProject).immutableCopy(),
                    substitutionFactory.getSubstitution(provenanceVariable, specialProvenanceConstant));

            return iqFactory.createUnaryIQTree(newConstructionNode, child);
        }

        public boolean isEmpty() {
            return leftChild.isDeclaredAsEmpty();
        }

        public IQTree createNormalizedTree(IQTreeCache treeCache) {
            if (leftChild.isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(projectedVariables);

            IQTreeCache normalizedProperties = treeCache.declareAsNormalizedForOptimizationWithEffect();

            IQTree ljLevelTree;
            if (rightChild.isDeclaredAsEmpty()) {
                Sets.SetView<Variable> rightSpecificVariables =
                        Sets.difference(rightChild.getVariables(), leftChild.getVariables());

                ConstructionNode newParentConstructionNode = iqFactory.createConstructionNode(
                        iqTreeTools.getChildrenVariables(leftChild, rightChild),
                        rightSpecificVariables.stream()
                                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));

                ljLevelTree = iqFactory.createUnaryIQTree(newParentConstructionNode, leftChild, normalizedProperties);
            }
            else if (rightChild.getRootNode() instanceof TrueNode) {
                ljLevelTree = leftChild;
            }
            else {
                ljLevelTree = iqFactory.createBinaryNonCommutativeIQTree(
                        iqFactory.createLeftJoinNode(ljCondition), leftChild, rightChild, normalizedProperties);
            }

            IQTree ancestorTree = ancestors.stream()
                    .reduce(ljLevelTree, (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            // Should not be called
                            (t1, t2) -> {
                                throw new MinorOntopInternalBugException("The order must be respected");
                            });

            IQTree nonNormalizedTree = iqTreeTools.createConstructionNodeTreeIfNontrivial(ancestorTree, projectedVariables);

            // Normalizes the ancestors (recursive)
            return nonNormalizedTree.normalizeForOptimization(variableGenerator);
        }

        /**
         * TODO: explain
         *
         * Right provenance variable: always there if needed
         *   (when some definitions do not depend on a right-specific variable)
         */
        private Substitution<ImmutableTerm> computeLiftableSubstitution(
                Substitution<? extends ImmutableTerm> selectedSubstitution,
                Optional<Variable> rightProvenanceVariable, ImmutableSet<Variable> leftVariables) {

            return selectedSubstitution.builder()
                    .removeFromDomain(leftVariables)
                    .transform(t -> transformRightSubstitutionValue(t, leftVariables, rightProvenanceVariable))
                    .build();
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

        private RightProvenance createProvenanceElements(Variable rightProvenanceVariable,
                                                                               IQTree rightTree, ImmutableSet<Variable> rightRequiredVariables) {

            ImmutableSet<Variable> newRightProjectedVariables = Sets.union(ImmutableSet.of(rightProvenanceVariable), rightRequiredVariables)
                    .immutableCopy();

            ConstructionNode newRightConstructionNode = iqFactory.createConstructionNode(
                    newRightProjectedVariables,
                    substitutionFactory.getSubstitution(rightProvenanceVariable, specialProvenanceConstant));

            UnaryIQTree newRightTree = iqFactory.createUnaryIQTree(newRightConstructionNode, rightTree);
            return new RightProvenance(rightProvenanceVariable, newRightTree);
        }

        /**
         * When at least one value does not depend on a right-specific variable
         *   (i.e. is a ground term or only depends on left variables)
         *
         *  A fresh non-nullable variable may have been introduced for the provenance inside a sparse data node
         *  of the returned right tree
         */
        private Optional<RightProvenance> createProvenanceElements(IQTree rightTree,
                                                                   Substitution<? extends ImmutableTerm> selectedSubstitution,
                                                                   ImmutableSet<Variable> leftVariables,
                                                                   ImmutableSet<Variable> rightRequiredVariables,
                                                                   VariableGenerator variableGenerator) {

            if (selectedSubstitution.removeFromDomain(leftVariables)
                    .rangeAnyMatch(t -> needsAnExternalProvenanceVariable(t, leftVariables))) {
                return Optional.of(rightProvenanceNormalizer.normalizeRightProvenance(
                        rightTree, leftVariables, rightRequiredVariables, variableGenerator));
            }
            else {
                return Optional.empty();
            }
        }

        /**
         * Return true when
         *   - the immutable term does NOT become null when the right child is rejected
         *   - AND the term is NOT capable of proposing its own provenance variable
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

        public LJNormalizationState propagateDownLJCondition() {
            if (ljCondition.isPresent()) {
                IQTree newRightChild = rightChild.propagateDownConstraint(ljCondition.get(), variableGenerator);
                return rightChild.equals(newRightChild)
                        ? this
                        : updateConditionAndRightChild(ljCondition, newRightChild);
            }
            else
                return this;
        }

        /**
         * If the right child does not contribute new variables and does not change the cardinality,
         * we can drop it
         */
        public LJNormalizationState checkRightChildContribution() {
            if (Sets.difference(rightChild.getVariables(), leftChild.getVariables()).isEmpty()
                    && (!rightChild.inferUniqueConstraints().isEmpty())) {
                TrueNode newRightChild = iqFactory.createTrueNode();
                return new LJNormalizationState(projectedVariables, leftChild, newRightChild, Optional.empty(),
                        ancestors, variableGenerator);
            }
            return this;
        }
    }
}
