package it.unibz.inf.ontop.executor.construction.impl;

import it.unibz.inf.ontop.executor.construction.ConstructionNodeRemovalExecutor;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;

public class ConstructionNodeRemovalExecutorImpl implements ConstructionNodeRemovalExecutor{

    @Override
    public NodeCentricOptimizationResults<ConstructionNode> apply(ConstructionNodeRemovalProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        throw new RuntimeException("TODO: implement");

    }
}
