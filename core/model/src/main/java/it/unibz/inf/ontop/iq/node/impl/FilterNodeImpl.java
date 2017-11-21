package it.unibz.inf.ontop.iq.node.impl;


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
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;

import java.util.Optional;

public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";
    private final IntermediateQueryFactory iqFactory;

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                           ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator);
        this.iqFactory = iqFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return new FilterNodeImpl(getOptionalFilterCondition().get(), getNullabilityEvaluator(), termFactory,
                typeFactory, datalogTools, createExpressionEvaluator(), iqFactory);
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
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }

    @Override
    public IQ liftBinding(IQ liftedChildIQ) {
        QueryNode childRoot = liftedChildIQ.getRootNode();
        if (childRoot instanceof ConstructionNode)
            return liftBinding((ConstructionNode) childRoot, (UnaryIQ) liftedChildIQ);
        else if (childRoot instanceof EmptyNode) {
            return liftedChildIQ;
        }
        else
            return iqFactory.createUnaryIQ(this, liftedChildIQ);
    }

    /**
     * TODO: simplify after getting rid of the former mechanism
     *
     * TODO: let the filter node simplify (interpret) expressions in the lifted substitution
     */
    private IQ liftBinding(ConstructionNode childConstructionNode, UnaryIQ liftedChildIQ) {
        IQ grandChildIQ = liftedChildIQ.getChild();

        SubstitutionResults<FilterNode> result = applySubstitution(childConstructionNode.getSubstitution());
        switch (result.getLocalAction()) {

            case NO_CHANGE:
                UnaryIQ filterIQ = iqFactory.createUnaryIQ(this, grandChildIQ);
                return iqFactory.createUnaryIQ(childConstructionNode, filterIQ);

            case NEW_NODE:
                UnaryIQ newFilterIQ = iqFactory.createUnaryIQ(result.getOptionalNewNode().get(), grandChildIQ);
                return iqFactory.createUnaryIQ(childConstructionNode, newFilterIQ);

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
