package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";
    private final ConstructionNodeTools constructionNodeTools;

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                           ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                           ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                           ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory,
                           ConstructionNodeTools constructionNodeTools) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools, defaultExpressionEvaluator);
        this.constructionNodeTools = constructionNodeTools;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return iqFactory.createFilterNode(getFilterCondition());
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
        return iqFactory.createFilterNode(newFilterCondition);
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
    public VariableNullability getVariableNullability(IQTree child) {
        return updateWithFilter(getFilterCondition(), child.getVariableNullability().getNullableGroups());
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
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFilter(tree,this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        checkExpression(getFilterCondition(), ImmutableList.of(child));
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return child.getPossibleVariableDefinitions();
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
            return liftBinding((ConstructionNode) childRoot, (UnaryIQTree) liftedChildIQTree, currentIQProperties, variableGenerator);
        else if (liftedChildIQTree.isDeclaredAsEmpty()) {
            return liftedChildIQTree;
        }
        else
            return iqFactory.createUnaryIQTree(this, liftedChildIQTree, currentIQProperties.declareLifted());
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child) {

        ImmutableExpression unoptimizedExpression = descendingSubstitution.applyToBooleanExpression(getFilterCondition());

        ImmutableSet<Variable> newlyProjectedVariables = constructionNodeTools
                .computeNewProjectedVariables(descendingSubstitution, child.getVariables());

        try {
            ExpressionAndSubstitution expressionAndSubstitution = simplifyCondition(Optional.of(unoptimizedExpression),
                    ImmutableSet.of());

            Optional<ImmutableExpression> downConstraint = computeDownConstraint(constraint,
                    expressionAndSubstitution);

            ImmutableSubstitution<? extends VariableOrGroundTerm> downSubstitution =
                    ((ImmutableSubstitution<VariableOrGroundTerm>)descendingSubstitution)
                            .composeWith2(expressionAndSubstitution.substitution);

            IQTree newChild = child.applyDescendingSubstitution(downSubstitution, downConstraint);
            IQTree filterLevelTree = expressionAndSubstitution.optionalExpression
                    .map(iqFactory::createFilterNode)
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, newChild))
                    .orElse(newChild);
            return expressionAndSubstitution.substitution.isEmpty()
                    ? filterLevelTree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(newlyProjectedVariables,
                                    (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)
                                            expressionAndSubstitution.substitution),
                            filterLevelTree);
        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(newlyProjectedVariables);
        }

    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        FilterNode newFilterNode = iqFactory.createFilterNode(
                descendingSubstitution.applyToBooleanExpression(getFilterCondition()));

        return iqFactory.createUnaryIQTree(newFilterNode,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution));
    }


    /**
     * TODO: let the filter node simplify (interpret) expressions in the lifted substitution
     */
    private IQTree liftBinding(ConstructionNode childConstructionNode, UnaryIQTree liftedChildIQ,
                               IQProperties currentIQProperties, VariableGenerator variableGenerator) {
        IQTree grandChildIQTree = liftedChildIQ.getChild();

        IQProperties liftedProperties = currentIQProperties.declareLifted();

        ImmutableExpression unoptimizedExpression = childConstructionNode.getSubstitution()
                .applyToBooleanExpression(getFilterCondition());

        try {
            ExpressionAndSubstitution expressionAndSubstitution = simplifyCondition(Optional.of(unoptimizedExpression),
                    ImmutableSet.of());

            Optional<FilterNode> newFilterNode = expressionAndSubstitution.optionalExpression
                    .map(iqFactory::createFilterNode);

            if (expressionAndSubstitution.substitution.isEmpty()) {
                IQTree filterLevelTree = newFilterNode
                        .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, grandChildIQTree, liftedProperties))
                        .orElse(grandChildIQTree);
                return iqFactory.createUnaryIQTree(childConstructionNode, filterLevelTree, liftedProperties);
            }
            else {
                IQTree newGrandChild = grandChildIQTree.applyDescendingSubstitution(
                        expressionAndSubstitution.substitution, expressionAndSubstitution.optionalExpression);

                IQTree filterLevelTree = newFilterNode
                        .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, newGrandChild, currentIQProperties))
                        .orElse(newGrandChild);

                IQTree filterParentTree = iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(grandChildIQTree.getVariables(),
                                (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)
                                        expressionAndSubstitution.substitution),
                        filterLevelTree);

                // Recursive
                return iqFactory.createUnaryIQTree(childConstructionNode, filterParentTree, currentIQProperties)
                        .liftBinding(variableGenerator);
            }

        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(childConstructionNode.getVariables());
        }
    }
}
