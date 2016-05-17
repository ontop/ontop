package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.*;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfLeftJoinExecutor implements NodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode> apply(LeftJoinOptimizationProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        throw new RuntimeException("TODO: implement it");
    }
}
