package it.unibz.inf.ontop.executor.union;

import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.UnionLiftProposal;

/**
 * TODO: explain
 */
public class UnionLiftInternalExecutor implements NodeCentricInternalExecutor<UnionNode, UnionLiftProposal> {

    @Override
    public NodeCentricOptimizationResults<UnionNode> apply(UnionLiftProposal proposal, IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        throw new RuntimeException("TODO: implement UnionLiftInternalExecutor");
    }
}
