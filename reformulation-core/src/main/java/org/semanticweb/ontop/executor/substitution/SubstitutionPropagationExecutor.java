package org.semanticweb.ontop.executor.substitution;

import org.semanticweb.ontop.executor.NodeCentricInternalExecutor;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

/**
 * TODO: explain
 */
public class SubstitutionPropagationExecutor
        implements NodeCentricInternalExecutor<QueryNode, SubstitutionPropagationProposal> {
    @Override
    public NodeCentricOptimizationResults<QueryNode> apply(SubstitutionPropagationProposal proposal,
                                                           IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        throw new RuntimeException("TODO: implement the substitution propagator");
    }
}
