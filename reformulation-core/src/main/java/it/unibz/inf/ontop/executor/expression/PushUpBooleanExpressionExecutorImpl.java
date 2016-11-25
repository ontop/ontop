package it.unibz.inf.ontop.executor.expression;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

public class PushUpBooleanExpressionExecutorImpl implements PushUpBooleanExpressionExecutor {
    @Override
    public NodeCentricOptimizationResults<CommutativeJoinOrFilterNode> apply(PushUpBooleanExpressionProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        ImmutableExpression expressionToPropagate = proposal.getFocusNode().getOptionalFilterCondition().
                orElseThrow(() -> new IllegalStateException("Invalid proposal: the focus node must provide a boolean expression"));

        /**
         * Extend the projections on the path from provider to recipient
         */
        for (ExplicitVariableProjectionNode projector : proposal.getInbetweenProjectors()) {
            Optional<ExplicitVariableProjectionNode> replacingProjector = getProjectorReplacementNode(projector, expressionToPropagate);
            if (replacingProjector.isPresent()) {
                treeComponent.replaceNode(projector, replacingProjector.get());
            }
        }

        /**
         * Create or update the recipient node
         */
        if (proposal.getRecipientNode().isPresent()) {
            JoinOrFilterNode replacingRecipient = getRecipientReplacementNode(proposal.getRecipientNode().get(), expressionToPropagate);
            treeComponent.replaceNode(proposal.getRecipientNode().get(), replacingRecipient);
        } else {
            QueryNode formerRootChild = query.getFirstChild(query.getRootConstructionNode())
                    .orElseThrow(() -> new IllegalStateException("This query is supposed to have more than two nodes"));
            treeComponent.insertParent(new FilterNodeImpl(expressionToPropagate), formerRootChild);
        }

        /**
         * Replace (JoinNode) or delete (FilterNode) the node providing the expression
         */
        Optional<CommutativeJoinOrFilterNode> replacingProvider = getProviderReplacementNode(proposal.getFocusNode());
        if (replacingProvider.isPresent()) {
            treeComponent.replaceNode(proposal.getFocusNode(), replacingProvider.get());
            return new NodeCentricOptimizationResultsImpl<>(query, replacingProvider.get());
        }
        QueryNode replacingChild = treeComponent.removeOrReplaceNodeByUniqueChild(proposal.getFocusNode());
        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(replacingChild));

    }


    private ImmutableExpression getCombinedExpression(ImmutableExpression expressionToPropagate, JoinOrFilterNode recipientNode) {
        Optional<ImmutableExpression> recipientNodeFormerExpression = recipientNode.getOptionalFilterCondition();
        if (recipientNodeFormerExpression.isPresent()) {
            return ImmutabilityTools.foldBooleanExpressions(recipientNodeFormerExpression.get(), expressionToPropagate)
                    .orElseThrow(() -> new IllegalStateException("Folding two existing expressions should produce an expression"));
        }
        return expressionToPropagate;
    }

    private Optional<ExplicitVariableProjectionNode> getProjectorReplacementNode(ExplicitVariableProjectionNode replacedNode, ImmutableExpression expressionToPropagate) {
        if (expressionToPropagate.getVariables().size() == 0) {
            return Optional.empty();
        }
        ImmutableSet.Builder<Variable> allProjectedVariablesBuilder = ImmutableSet.builder();
        allProjectedVariablesBuilder.addAll(replacedNode.getVariables());
        allProjectedVariablesBuilder.addAll(expressionToPropagate.getVariables());

        if (replacedNode instanceof UnionNode) {
            return Optional.of(new UnionNodeImpl(allProjectedVariablesBuilder.build()));
        } else if (replacedNode instanceof ConstructionNode) {
            return Optional.of(new ConstructionNodeImpl(allProjectedVariablesBuilder.build(),
                    ((ConstructionNode) replacedNode).getSubstitution(),
                    ((ConstructionNode) replacedNode).getOptionalModifiers()));
        } else {
            throw new IllegalStateException("unsupported node type");
        }
    }

    private JoinOrFilterNode getRecipientReplacementNode(JoinOrFilterNode replacedNode, ImmutableExpression expressionToPropagate) {
        ImmutableExpression combinedExpression = getCombinedExpression(expressionToPropagate, replacedNode);
        if (replacedNode instanceof InnerJoinNode) {
            return new InnerJoinNodeImpl(Optional.of(combinedExpression));
        } else if (replacedNode instanceof LeftJoinNode) {
                return new LeftJoinNodeImpl(Optional.of(combinedExpression));
        } else if (replacedNode instanceof FilterNode) {
            return new FilterNodeImpl(combinedExpression);
        } else {
            throw new IllegalStateException("Invalid proposal: A CommutativeJoinOrFilterNode must be a commutative join or filter node");
        }
    }

    private Optional<CommutativeJoinOrFilterNode> getProviderReplacementNode(CommutativeJoinOrFilterNode providerNode) {
        if (providerNode instanceof InnerJoinNode) {
            return Optional.of(new InnerJoinNodeImpl(Optional.empty()));
        } else if (providerNode instanceof FilterNode) {
            return Optional.empty();
        } else {
            throw new IllegalStateException("Invalid proposal: A CommutativeJoinOrFilterNode must be a commutative join or filter node");
        }
    }
}
