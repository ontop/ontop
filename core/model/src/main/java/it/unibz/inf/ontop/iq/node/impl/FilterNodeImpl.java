package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.Optional;
import java.util.stream.Stream;

public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                           ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                           ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                           ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools, defaultExpressionEvaluator);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return new FilterNodeImpl(getOptionalFilterCondition().get(), getNullabilityEvaluator(), termFactory,
                typeFactory, datalogTools, immutabilityTools, substitutionFactory, unificationTools, substitutionTools,
                createExpressionEvaluator(), iqFactory);
    }

    @Override
    public FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public FilterNode changeFilterCondition(ImmutableExpression newFilterCondition) {
        return new FilterNodeImpl(newFilterCondition, getNullabilityEvaluator(), termFactory, typeFactory, datalogTools,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools,
                createExpressionEvaluator(), iqFactory);
    }

    @Override
    public SubstitutionResults<FilterNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return applySubstitution(substitution);
    }

    @Override
    public SubstitutionResults<FilterNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            IntermediateQuery query) {
        return applySubstitution(substitution);
    }

    private SubstitutionResults<FilterNode> applySubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        EvaluationResult evaluationResult = transformBooleanExpression(substitution, getFilterCondition());

        /*
         * The condition cannot be satisfied --> the sub-tree is empty.
         */
        if (evaluationResult.isEffectiveFalse()) {
            return DefaultSubstitutionResults.declareAsEmpty();
        }
        else {
            /*
             * Propagates the substitution and ...
             */
            return evaluationResult.getOptionalExpression()
                    /*
                     * Still a condition: returns a filter node with the new condition
                     */
                    .map(exp -> DefaultSubstitutionResults.newNode(changeFilterCondition(exp), substitution))
                    /*
                     * No condition: the filter node is not needed anymore
                     */
                    .orElseGet(() -> DefaultSubstitutionResults.replaceByUniqueChild(substitution));
        }
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (isFilteringNullValue(variable))
            return false;

        return query.getFirstChild(this)
                .map(c -> c.isVariableNullable(query, variable))
                .orElseThrow(() -> new InvalidIntermediateQueryException("A filter node must have a child"));
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree child) {
        return child.getNullableVariables().stream()
                .filter(v -> !isFilteringNullValue(v))
                .collect(ImmutableCollectors.toSet());
    }


    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the filter
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return propagateDownCondition(child, Optional.of(constraint));
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof FilterNode)
                && ((FilterNode) node).getFilterCondition().equals(this.getFilterCondition());
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY, emptyChild.getVariables());
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        throw new UnsupportedOperationException("The TrueNode child of a FilterNode is not expected to be removed");
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof FilterNode)
                && getFilterCondition().equals(((FilterNode) queryNode).getFilterCondition());
    }

    @Override
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newParentTree = propagateDownCondition(childIQTree, Optional.empty());

        /*
         * If after propagating down the condition the root is still a filter node, goes to the next step
         */
        if (newParentTree.getRootNode() instanceof FilterNodeImpl) {
            return ((FilterNodeImpl)newParentTree.getRootNode())
                    .liftBindingAfterPropagatingCondition(((UnaryIQTree)newParentTree).getChild(),
                            variableGenerator, currentIQProperties);
        }
        else
            /*
             * Otherwise, goes back to the general method
             */
            return newParentTree.liftBinding(variableGenerator);
    }

    private IQTree propagateDownCondition(IQTree child, Optional<ImmutableExpression> initialConstraint) {
        try {
            ExpressionAndSubstitution conditionSimplificationResults =
                    simplifyCondition(Optional.of(getFilterCondition()), ImmutableSet.of());

            Optional<ImmutableExpression> downConstraint = computeDownConstraint(initialConstraint,
                    conditionSimplificationResults);

            IQTree newChild = Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> child.applyDescendingSubstitution(s, downConstraint))
                    .orElseGet(() -> downConstraint
                            .map(child::propagateDownConstraint)
                            .orElse(child));

            IQTree filterLevelTree = conditionSimplificationResults.optionalExpression
                    .map(e -> e.equals(getFilterCondition()) ? this : iqFactory.createFilterNode(e))
                    .map(filterNode -> (IQTree) iqFactory.createUnaryIQTree(filterNode, newChild))
                    .orElse(newChild);

            return Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)s)
                    .map(s -> iqFactory.createConstructionNode(child.getVariables(), s))
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, filterLevelTree))
                    .orElse(filterLevelTree);


        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(child.getVariables());
        }

    }

    private IQTree liftBindingAfterPropagatingCondition(IQTree childIQTree, VariableGenerator variableGenerator,
                                                        IQProperties currentIQProperties) {
        IQTree liftedChildIQTree = childIQTree.liftBinding(variableGenerator);
        QueryNode childRoot = liftedChildIQTree.getRootNode();
        if (childRoot instanceof ConstructionNode)
            return liftBinding((ConstructionNode) childRoot, (UnaryIQTree) liftedChildIQTree, currentIQProperties);
        else if (liftedChildIQTree.isDeclaredAsEmpty()) {
            return liftedChildIQTree;
        }
        else
            return iqFactory.createUnaryIQTree(this, liftedChildIQTree, currentIQProperties.declareLifted());
    }

    /**
     * TODO: consider the constraint
     */
    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child) {

        SubstitutionResults<FilterNode> results = applySubstitution(descendingSubstitution);

        switch (results.getLocalAction()) {
            case NO_CHANGE:
                return iqFactory.createUnaryIQTree(this,
                        child.applyDescendingSubstitution(descendingSubstitution, constraint));
            case NEW_NODE:
                return iqFactory.createUnaryIQTree(results.getOptionalNewNode().get(),
                        child.applyDescendingSubstitution(descendingSubstitution, constraint));
            case REPLACE_BY_CHILD:
                return child.applyDescendingSubstitution(descendingSubstitution, constraint);
            case DECLARE_AS_EMPTY:
                return iqFactory.createEmptyNode(child.getVariables());
            default:
                throw new MinorOntopInternalBugException("Unexpected local action: " + results.getLocalAction());
        }
    }


    /**
     * TODO: simplify after getting rid of the former mechanism
     *
     * TODO: let the filter node simplify (interpret) expressions in the lifted substitution
     */
    private IQTree liftBinding(ConstructionNode childConstructionNode, UnaryIQTree liftedChildIQ,
                               IQProperties currentIQProperties) {
        IQTree grandChildIQTree = liftedChildIQ.getChild();

        IQProperties liftedProperties = currentIQProperties.declareLifted();

        SubstitutionResults<FilterNode> result = applySubstitution(childConstructionNode.getSubstitution());
        switch (result.getLocalAction()) {

            case NO_CHANGE:
                UnaryIQTree filterIQ = iqFactory.createUnaryIQTree(this, grandChildIQTree);
                return iqFactory.createUnaryIQTree(childConstructionNode, filterIQ, liftedProperties);

            case NEW_NODE:
                UnaryIQTree newFilterIQ = iqFactory.createUnaryIQTree(result.getOptionalNewNode().get(), grandChildIQTree,
                        liftedProperties);
                return iqFactory.createUnaryIQTree(childConstructionNode, newFilterIQ, liftedProperties);

            case REPLACE_BY_CHILD:
                return liftedChildIQ;

            case DECLARE_AS_EMPTY:
                return iqFactory.createEmptyNode(liftedChildIQ.getVariables());

            default:
                throw new MinorOntopInternalBugException("Unexpected action for propagating a substitution in a FilterNode: "
                        + result.getLocalAction());
        }
    }
}
