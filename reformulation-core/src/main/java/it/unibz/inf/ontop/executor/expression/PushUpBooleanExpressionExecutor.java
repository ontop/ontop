package it.unibz.inf.ontop.executor.expression;

import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushUpBooleanExpressionResults;

public interface PushUpBooleanExpressionExecutor extends InternalProposalExecutor<PushUpBooleanExpressionProposal, ProposalResults>{

}
