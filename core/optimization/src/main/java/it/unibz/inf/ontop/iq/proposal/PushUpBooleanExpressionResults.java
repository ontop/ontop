package it.unibz.inf.ontop.iq.proposal;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;

public interface PushUpBooleanExpressionResults extends ProposalResults{
    /**
     *  All replacement nodes for the nodes initially providing the expression propagated up.
     *  A replacement node for a provider p may be either p with a weaker filter,
     *  or the unique child p if p was a FilterNode and p's whole condition has been pushed up
     */
    ImmutableSet<QueryNode> getExpressionProviderReplacingNodes();

    IntermediateQuery getResultingQuery();
}
