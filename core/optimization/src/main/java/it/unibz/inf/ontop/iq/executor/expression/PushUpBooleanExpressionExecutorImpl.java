package it.unibz.inf.ontop.iq.executor.expression;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.*;
import it.unibz.inf.ontop.iq.proposal.*;
import it.unibz.inf.ontop.iq.proposal.impl.PushUpBooleanExpressionResultsImpl;

import java.util.Map;
import java.util.Optional;

public class PushUpBooleanExpressionExecutorImpl implements PushUpBooleanExpressionExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private PushUpBooleanExpressionExecutorImpl(IntermediateQueryFactory iqFactory, ImmutabilityTools immutabilityTools) {
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public PushUpBooleanExpressionResults apply(PushUpBooleanExpressionProposal proposal, IntermediateQuery query,
                                                QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        ImmutableExpression expressionToPropagate = proposal.getPropagatedExpression();

        /**
         * Create or update the recipient node
         */
        if (proposal.getRecipientNode().isPresent()) {
            JoinOrFilterNode replacingRecipient = getRecipientReplacementNode(proposal.getRecipientNode().get(), expressionToPropagate);
            treeComponent.replaceNode(proposal.getRecipientNode().get(), replacingRecipient);
        } else {
            treeComponent.insertParent(proposal.getUpMostPropagatingNode(), iqFactory.createFilterNode(expressionToPropagate));
        }

        /**
         * Extend the projections on the path from provider to blocking node
         */
        for (ExplicitVariableProjectionNode projector : proposal.getInbetweenProjectors()) {
            Optional<ExplicitVariableProjectionNode> replacingProjector = getProjectorReplacementNode(projector, expressionToPropagate);
            if (replacingProjector.isPresent()) {
                treeComponent.replaceNode(projector, replacingProjector.get());
            }
        }


        /**
         * Replace or delete the nodes providing the expression
         */
        ImmutableSet.Builder<QueryNode> providerReplacementNodesBuilder = ImmutableSet.builder();
        for (Map.Entry<CommutativeJoinOrFilterNode, Optional<ImmutableExpression>> entry : proposal.getProvider2NonPropagatedExpressionMap().entrySet()) {
            Optional<CommutativeJoinOrFilterNode> replacingProvider = getProviderReplacementNode(entry.getKey(),
                    entry.getValue());
            if (replacingProvider.isPresent()) {
                treeComponent.replaceNode(entry.getKey(), replacingProvider.get());
                providerReplacementNodesBuilder.add(replacingProvider.get());
            } else {
                providerReplacementNodesBuilder.add(treeComponent.removeOrReplaceNodeByUniqueChild(entry.getKey()));
            }
        }
        return new PushUpBooleanExpressionResultsImpl(providerReplacementNodesBuilder.build(), query);
    }


    private ImmutableExpression getCombinedExpression(ImmutableExpression expressionToPropagate, JoinOrFilterNode recipientNode) {
        Optional<ImmutableExpression> recipientNodeFormerExpression = recipientNode.getOptionalFilterCondition();
        if (recipientNodeFormerExpression.isPresent()) {
            return immutabilityTools.foldBooleanExpressions(recipientNodeFormerExpression.get(), expressionToPropagate)
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
            return Optional.of(iqFactory.createUnionNode(allProjectedVariablesBuilder.build()));
        }
        if (replacedNode instanceof ConstructionNode) {
            return Optional.of(iqFactory.createConstructionNode(allProjectedVariablesBuilder.build(),
                    ((ConstructionNode) replacedNode).getSubstitution()));
        }
        throw new IllegalStateException("Unsupported node type");

    }

    private JoinOrFilterNode getRecipientReplacementNode(JoinOrFilterNode replacedNode, ImmutableExpression expressionToPropagate) {
        ImmutableExpression combinedExpression = getCombinedExpression(expressionToPropagate, replacedNode);
        if (replacedNode instanceof InnerJoinNode)
            return iqFactory.createInnerJoinNode(Optional.of(combinedExpression));
        if (replacedNode instanceof LeftJoinNode)
            return iqFactory.createLeftJoinNode(Optional.of(combinedExpression));
        if (replacedNode instanceof FilterNode)
            return iqFactory.createFilterNode(combinedExpression);
        throw new IllegalStateException("Invalid proposal: A CommutativeJoinOrFilterNode must be a commutative join or filter node");
    }

    private Optional<CommutativeJoinOrFilterNode> getProviderReplacementNode(CommutativeJoinOrFilterNode providerNode,
                                                                             Optional<ImmutableExpression> nonPropagatedExpression) {
        if (providerNode instanceof InnerJoinNode) {
            return Optional.of(iqFactory.createInnerJoinNode(nonPropagatedExpression));
        }
        if (providerNode instanceof FilterNode) {
            if (nonPropagatedExpression.isPresent()) {
                return Optional.of(iqFactory.createFilterNode(nonPropagatedExpression.get()));
            }
            return Optional.empty();
        }
        throw new IllegalStateException("Invalid proposal: A CommutativeJoinOrFilterNode must be a commutative " +
                "join or filter node");
    }
}
