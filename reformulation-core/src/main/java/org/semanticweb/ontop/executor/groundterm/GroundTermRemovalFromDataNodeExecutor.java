package org.semanticweb.ontop.executor.groundterm;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.model.GroundTerm;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.ImmutabilityTools;
import org.semanticweb.ontop.pivotalrepr.DataNode;
import org.semanticweb.ontop.pivotalrepr.FilterNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.JoinOrFilterNode;
import org.semanticweb.ontop.pivotalrepr.impl.FilterNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.GroundTermRemovalFromDataNodeProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;

import java.util.Collection;

/**
 * TODO: explain
 */
public class GroundTermRemovalFromDataNodeExecutor implements
        InternalProposalExecutor<GroundTermRemovalFromDataNodeProposal, ProposalResults> {


    private static class VariableGroundTermPair {
        public final Variable variable;
        public final GroundTerm groundTerm;

        private VariableGroundTermPair(Variable variable, GroundTerm groundTerm) {
            this.variable = variable;
            this.groundTerm = groundTerm;
        }
    }

    /**
     * TODO: explain
     */
    @Override
    public ProposalResults apply(GroundTermRemovalFromDataNodeProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException {

        ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> receivingNodes = processDataNodes(
                proposal.getDataNodesToSimplify(), query, treeComponent);

        processJoinOrFilterNodes(receivingNodes, treeComponent);

        return new ProposalResultsImpl(query);
    }

    /**
     * TODO: explain
     */
    private ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> processDataNodes(
            ImmutableList<DataNode> dataNodesToSimplify, IntermediateQuery query, QueryTreeComponent treeComponent) {
        ImmutableMultimap.Builder<JoinOrFilterNode, VariableGroundTermPair> multimapBuilder = ImmutableMultimap.builder();

        for (DataNode dataNode : dataNodesToSimplify) {
            Optional<JoinOrFilterNode> optionalReceivingNode = findClosestJoinOrFilterNode(query, dataNode);
            ImmutableList<VariableGroundTermPair> pairs = extractPairs(dataNode);

            if (optionalReceivingNode.isPresent()) {
                for (VariableGroundTermPair pair : pairs) {
                    multimapBuilder.put(optionalReceivingNode.get(), pair);
                }
            }
            /**
             * TODO: explain
             */
            else {
                ImmutableBooleanExpression joiningCondition = convertIntoBooleanExpression(pairs);
                FilterNode newFilterNode = new FilterNodeImpl(joiningCondition);
                try {
                    treeComponent.insertParent(dataNode, newFilterNode);
                } catch (IllegalTreeUpdateException e) {
                    throw new RuntimeException("Unexpected exception: " + e);
                }
            }
        }

        return multimapBuilder.build();
    }

    private ImmutableBooleanExpression convertIntoBooleanExpression(Collection<VariableGroundTermPair> pairs) {
        throw new RuntimeException("TODO: implement it");
    }

    private ImmutableList<VariableGroundTermPair> extractPairs(DataNode dataNode) {
        throw new RuntimeException("TODO: implement it");
    }

    private Optional<JoinOrFilterNode> findClosestJoinOrFilterNode(IntermediateQuery query, DataNode dataNode) {
        throw new RuntimeException("TODO: implement it");
    }

    private void processJoinOrFilterNodes(ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> receivingNodes,
                                          QueryTreeComponent treeComponent) {
        throw new RuntimeException("TODO: implement it");
    }
}
